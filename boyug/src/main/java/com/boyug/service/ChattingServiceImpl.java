package com.boyug.service;

import com.boyug.dto.BoyugUserDto;
import com.boyug.dto.ChatMessageDto;
import com.boyug.dto.ChatRoomDto;
import com.boyug.dto.UserDto;
import com.boyug.entity.ChatMessageEntity;
import com.boyug.entity.ChatRoomEntity;
import com.boyug.repository.ChatMessageRepository;
import com.boyug.repository.ChatRoomRepository;
import com.boyug.security.WebUserDetails;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChattingServiceImpl implements ChattingService {

    @Setter
    ChatRoomRepository chatRoomRepository;

    @Setter
    ChatMessageRepository chatMessageRepository;

    @Setter
    AccountService accountService;

    // NotificationService 와 순환 참조로 인해 이벤트 객체 생성
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    private static final String WELCOME_MESSAGE = "안녕하세요 문의사항을 남겨주시면 확인 후 답변드리겠습니다.";


    @Override
    public int getNextChatRoomId() {
        return chatRoomRepository.findNextChatRoomId();
    }


    /*
    * chatRoom 생성
    * */
    @Override
    public List<ChatRoomDto> createOrGetChatRoom(WebUserDetails userDetails, int toUserId) {
        List<ChatRoomDto> roomList = new ArrayList<>();
        Integer existingRoom = getRoomDupCheck(userDetails.getUser(), toUserId);

        if (existingRoom == null) {
            ChatRoomDto newRoom = createNewChatRoom(userDetails, toUserId);
            roomList.add(newRoom);
            sendWelcomeMessage(newRoom, userDetails, toUserId);
        } else {
            ChatRoomDto existingChatRoom = getChatRoom(existingRoom);
            existingChatRoom.setRoomNumber(existingRoom);
            roomList.add(existingChatRoom);
        }
        return roomList;
    }


    public Integer getRoomDupCheck(UserDto loginUser, int otherUserId) {
        Integer isChatRoom = 0;
        if(loginUser.getUserCategory() == 2) {
            isChatRoom = chatMessageRepository.findChatRoomByUserIdsAndBoyugType(loginUser.getUserId(), otherUserId);
        } else if(loginUser.getUserCategory() == 3) {
            isChatRoom = chatMessageRepository.findChatRoomByUserIdsAndUserType(loginUser.getUserId(), otherUserId);
        }

        return isChatRoom;
    }

    private ChatRoomDto createNewChatRoom(WebUserDetails userDetails, int toUserId) {
        ChatRoomDto room = new ChatRoomDto();
        ChatRoomEntity savedEntity = chatRoomRepository.save(room.toEntity());
        int savedId = savedEntity.getChatRoomId();

        room.setChatRoomId(savedId);
        room.setRoomNumber(savedId);
        room.setToUserId(toUserId);
        room.setFromUserId(userDetails.getUser().getUserId());

        return room;
    }

    private void sendWelcomeMessage(ChatRoomDto chatRoom, WebUserDetails fromUser, int toUserId) {
        UserDto toUser = accountService.getUserInfo(toUserId);

        ChatMessageDto sendMessage = new ChatMessageDto();
        sendMessage.setChatRoomId(chatRoom);
        sendMessage.setToUserId(fromUser.getUser());
        sendMessage.setFromUserId(toUser);
        sendMessage.setChatContent(WELCOME_MESSAGE);

        insertChatMessage(sendMessage);
    }


    @Override
    public ChatRoomDto getChatRoom(int roomNumber) {
        return chatRoomRepository.findById(roomNumber)
                .map(ChatRoomDto::of)
                .orElse(null);
    }




    @Override
    public void sendMessage(WebUserDetails userDetails, String roomNumber, String message, int toUserId) {
        UserDto toUser = accountService.getUserInfo(toUserId);

        ChatRoomDto chatRoom = getChatRoom(Integer.parseInt(roomNumber));
        ChatMessageDto sendMessage = new ChatMessageDto();
        sendMessage.setChatRoomId(chatRoom);
        sendMessage.setToUserId(toUser);
        sendMessage.setFromUserId(userDetails.getUser());
        sendMessage.setChatContent(message);

        // 메세지 저장
        insertChatMessage(sendMessage);

        // 알림 메세지 생성 + 발송
        newNotificationMessage(userDetails, toUser, chatRoom, message);

    }

    public void newNotificationMessage(WebUserDetails userDetails, UserDto toUser,
                                       ChatRoomDto chatRoom, String message) {
        // 알림 메세지 생성
        String notificationMessage;

        if(userDetails.getUser().getUserCategory() == 2) {
            BoyugUserDto boyugUser = accountService.getBoyugUserInfo(userDetails.getUser().getUserId());
            notificationMessage = String.format("%s님이 메세지를 보냈습니다.", boyugUser.getBoyugUserName());
        } else {
            notificationMessage = String.format("%s님이 메세지를 보냈습니다.", userDetails.getUser().getUserName());
        }

        // 알림발송 -> 순환참조 때문에 이벤트 만듬
        eventPublisher.publishEvent(new ChattingEvent(userDetails.getUser(), toUser, chatRoom, notificationMessage, message));
    }



    @Override
    public void insertChatMessage(ChatMessageDto sendMessage) {
        ChatMessageEntity entity = sendMessage.toEntity();
        chatMessageRepository.save(entity);
    }

    @Override
    public List<ChatMessageDto> getMessagesByRoomId(int chatRoomId) {
        List<ChatMessageEntity> entities = chatMessageRepository.findByChatRoomChatRoomIdOrderByChatSendTimeAsc(chatRoomId);
        return entities.stream()
                .map(ChatMessageDto::of)
                .collect(Collectors.toList());
    }

    @Override
    public void updateChatMessageIsRead(int chatRoomId, int toUserId) {
        chatMessageRepository.updateToIsRead(chatRoomId, toUserId);
    }

    @Override
    public List<ChatRoomDto> getChatRoomByUserId(UserDto loginUser) {
        List<ChatRoomEntity> list = null;

        if(loginUser.getUserCategory() == 2) { // 보육유저
            list = chatRoomRepository.findAllChatRoomByBoyugUserId(loginUser.getUserId());
        } else if(loginUser.getUserCategory() == 3) { // 개인회원
            list = chatRoomRepository.findAllChatRoomByUserId(loginUser.getUserId());
        }

        if(list != null) {
            for (ChatRoomEntity chatRoomEntity : list) { // 한 행씩 꺼내서 보여줌 == 펼침.
                List<ChatMessageEntity> messages = chatMessageRepository.findByChatRoomChatRoomIdOrderByChatSendTimeAsc(chatRoomEntity.getChatRoomId());
                chatRoomEntity.setChatMessages(messages);
            }
        }
        return list != null ? list.stream()
                                  .map(ChatRoomDto::of)
                                  .collect(Collectors.toList()) : null;
    }

    @Override
    public void editChatRoomActive(WebUserDetails user, int[] selectedValues) {

        List<ChatRoomEntity> entityList = new ArrayList<>();
        for (int result : selectedValues) {
            ChatRoomEntity entity = chatRoomRepository.findById(result).orElseThrow();
            entityList.add(entity);
        }

        if(user.getUser().getUserCategory() == 2) {
            for (ChatRoomEntity chatRoomEntity : entityList) {
                chatRoomEntity.setBoyugChatActive(false);
            }

        } else if ((user.getUser().getUserCategory() == 3)) {
            for (ChatRoomEntity chatRoomEntity : entityList) {
                chatRoomEntity.setUserChatActive(false);
            }
        }

        for (ChatRoomEntity chatRoomEntity : entityList) {
            chatRoomRepository.save(chatRoomEntity);
        }
    }

}
