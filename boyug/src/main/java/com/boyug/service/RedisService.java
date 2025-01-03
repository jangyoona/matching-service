package com.boyug.service;

import com.boyug.dto.ChatMessageDto;
import com.boyug.entity.ChatMessageEntity;
import com.boyug.repository.AccountRepository;
import com.boyug.repository.ChatMessageRepository;
import com.boyug.repository.ChatRoomRepository;
import com.boyug.websocket.SocketHandler;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;


    /**
     * Chatting Message Temporary save
     **/
    // Add Message
    public void addMessageTemporary(ChatMessageDto message) {
        String result = String.format("%s|%s|%s|%s|%s", message.getChatRoomId().getChatRoomId(),    // ChatRoomId
                                                        message.getChatContent(),                   // ChatContent
                                                        new Timestamp(System.currentTimeMillis()),  // ChatSendTime
                                                        message.getFromUserId().getUserId(),        // FromUserId
                                                        message.getToUserId().getUserId());         // ToUserId
        redisTemplate.opsForList().rightPush("messages:" + message.getChatRoomId().getChatRoomId(), result);
    }

    // All Messages 조회
    public Set<String> findAllMessages() {
        return redisTemplate.keys("messages:*");
    }

    // Messages 길이 조회 By ChatRoomId
    public Long getMessagesSize(String chatRoomId) {
        return redisTemplate.opsForList().size(chatRoomId);
    }

    // Messages 조회 By ChatRoomId
    public List<String> findMessagesByChatRoomId(String chatRoomId) {
        return redisTemplate.opsForList().range(chatRoomId, 0, -1);
    }

    // Messages DB 저장 후 삭제
    public void deleteMessagesByChatRoomId(String chatRoomId) {
        redisTemplate.delete(chatRoomId);
    }


    /**
     * Chatting User
     **/
    // 채팅방 userID 저장
    public void addUserToRoomNumber(String roomNumber, String userId) {
        redisTemplate.opsForSet().add("room:" + roomNumber, userId);
//        redisTemplate.expire("room:" + roomNumber, 30, TimeUnit.MINUTES); // 30분 후 자동 삭제
    }

    // 해당 RoomNumber가 존재하는지 조회
    public boolean isRoomExists(String roomNumber) {
        Boolean exists = redisTemplate.hasKey("room:" + roomNumber);
        return exists != null && exists;
    }

    // 해당 유저가 채팅방에 있는지 조회
    public boolean isUserInRoomNumber(String roomNumber, String userId) {
        return redisTemplate.opsForSet().isMember("room:" + roomNumber, userId);
    }

    // 채팅방 사용자 제거
    public void removeUserFromRoomNumber(String roomNumber, String userId) {
        redisTemplate.opsForSet().remove("room:" + roomNumber, userId);
    }


    /**
     * Chatting Message Pub/Sub 전송
     **/
    public void sendMessage(ChatMessageDto message) {

        JSONObject obj = new JSONObject();
        obj.put("roomNumber", message.getChatRoomId().getChatRoomId());
        obj.put("message", message.getChatContent());
        obj.put("fromUserId", message.getFromUserId().getUserId());
        obj.put("toUserId", message.getToUserId().getUserId());
        obj.put("chatSendTime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .format(new Date())); // 문자열로 변환

        String roomNumber = obj.get("roomNumber").toString();

        // 1. 상대방에게 메세지 발행
        String channel1 = roomNumber + ":" + message.getToUserId().getUserId();
        redisTemplate.convertAndSend(channel1, obj.toString());

        // 2. 나에게도 메세지 발행
        String channel2 = roomNumber + ":" + message.getFromUserId().getUserId();
        redisTemplate.convertAndSend(channel2, obj.toString());

//        boolean status = SocketHandler.getSessions().containsKey(message.getChatRoomId().getChatRoomId() + ":" + message.getToUserId().getUserId()); // 이건 서버에서 찾는거
        boolean userStatus = isUserInRoomNumber(roomNumber, obj.get("toUserId").toString());
        if (!userStatus) {
            // 해당 채팅방에 상대방 없으면 이 전 대화 복구를 위해 Redis 임시저장본 -> DB 저장
            convertAndSaveToMessages(roomNumber);
        }
    }

    private void convertAndSaveToMessages(String roomNumber) {
        List<String> messages = findMessagesByChatRoomId("messages:" + roomNumber);

        if (messages != null && !messages.isEmpty()) {
            // 메세지 Entity 로 변환
            List<ChatMessageEntity> entity = convertMessagesToEntities(messages);

            // 임시 저장본 DB 일괄 저장
            chatMessageRepository.saveAll(entity);

            // 저장 후 Redis 데이터 삭제
            deleteMessagesByChatRoomId("messages:" + roomNumber);
        }
    }

    @NotNull
    private List<ChatMessageEntity> convertMessagesToEntities(List<String> messages) {
        return messages.stream().map(msg -> {
            String[] parts = msg.split("\\|");

            return new ChatMessageEntity(
                    chatRoomRepository.findById(Integer.parseInt(parts[0])), // chatRoomId
                    parts[1],                                                // chatContent
                    Timestamp.valueOf(parts[2]),                             // chatSendTime
                    accountRepository.findById(Integer.parseInt(parts[3])),  // fromUser
                    accountRepository.findById(Integer.parseInt(parts[4])),  // toUser
                    false                                                    // toIsRead
            );
        }).collect(Collectors.toList());
    }


    /**
     * JWT Refresh Token
     **/
    // Jwt Refresh Token 저장
    public void addJwtRefreshToken(int userId, String refreshToken) {
        removeJwtRefreshTokenToUserId(userId); // 기존 RefreshToken 삭제하고
        redisTemplate.opsForSet().add("refreshToken:" + userId, refreshToken);
    }

    // Jwt Refresh Token 조회
    public String getJwtRefreshTokenToUserId(int userId) {
        Set<String> refreshToken = redisTemplate.opsForSet().members("refreshToken:" + userId);
        return refreshToken != null && !refreshToken.isEmpty() ? refreshToken.iterator().next() : null;
    }

    // Jwt Refresh Token 삭제
    public void removeJwtRefreshTokenToUserId(int userId) {
        redisTemplate.delete("refreshToken:" + userId);
    }


    private void saveMessagesToDB(Set<String> messageKeys) {

        for (String messageKey : messageKeys) {

            List<String> messages = findMessagesByChatRoomId(messageKey);

            if (messages != null && !messages.isEmpty()) {
                // 메세지 Entity 로 변환
                List<ChatMessageEntity> entity = messages.stream().map(msg -> {
                    String[] parts = msg.split("\\|");

                    return new ChatMessageEntity(
                            chatRoomRepository.findById(Integer.parseInt(parts[0])), // chatRoomId
                            parts[1],                                                // chatContent
                            Timestamp.valueOf(parts[2]),                             // chatSendTime
                            accountRepository.findById(Integer.parseInt(parts[3])),  // fromUser
                            accountRepository.findById(Integer.parseInt(parts[4])),  // toUser
                            false                                                    // toIsRead
                    );
                }).collect(Collectors.toList());

                // DB 일괄 저장
                chatMessageRepository.saveAll(entity);

                // 저장 후 Redis 데이터 삭제
                redisTemplate.delete(messageKey);
            }
        }
    }

    /**
     * 서버 초기화용 roomNumber~ and messages~ 삭제
     **/
    public void initRedis() {
        // 1. 채팅방 키 삭제
        Set<String> roomKeys = redisTemplate.keys("room:*");
        if (roomKeys != null && !roomKeys.isEmpty()) {
            redisTemplate.delete(roomKeys);
        } else {
            System.out.println("RedisService = No roomNumber keys");
        }

        // 2. 메세지 있다면 저장 후 삭제
        Set<String> messageKeys = redisTemplate.keys("messages:*");
        if (messageKeys != null && !messageKeys.isEmpty()) {
            // DB 저장 안된채로 서버 종료된 경우 잽싸게 저장 -> Redis 초기화
            saveMessagesToDB(messageKeys);
        } else {
            System.out.println("RedisService = No message keys");
        }
    }


}
