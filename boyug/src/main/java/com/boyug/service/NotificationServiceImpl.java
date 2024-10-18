package com.boyug.service;

import com.boyug.dto.BoyugUserDto;
import com.boyug.dto.ChatRoomDto;
import com.boyug.dto.NotificationDto;
import com.boyug.dto.UserDto;
import com.boyug.entity.NotificationEntity;
import com.boyug.repository.NotificationRepository;
import com.boyug.websocket.SocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class NotificationServiceImpl implements NotificationService {

    @Setter
    private NotificationRepository notificationRepository;

    @Setter(onMethod_ = { @Autowired})
    ChattingService chattingService;

    @Setter(onMethod_ = { @Autowired})
    AccountService accountService;

    private static Map<String, SseEmitter> emitters = new HashMap<>();


    @Override
    public void insertNotification(UserDto fromUser, UserDto toUser, ChatRoomDto chatRoom, String notificationMessage, String message2) {
        // 알림 DB 저장
        NotificationEntity notification = new NotificationEntity();
        notification.setFromUser(fromUser.toEntity());
        notification.setToUser(toUser.toEntity());
        notification.setChatRoom(chatRoom.toEntity());
        notification.setMessage(notificationMessage);
        NotificationEntity savedNotificationId = notificationRepository.save(notification);

        // SSE 알림 발송
        List<HashMap<String, Object>> connectedUsers = SocketHandler.getRoomConnectUser(); // 채팅창 안에 있는 사람들 저장 list_map
        List<String> users = SocketHandler.getUsers();

        // 상대방이 채팅방 안에 있는지 구분
        if(connectedUsers != null) {
            boolean isUserInRoom = users.stream()
                    .anyMatch(userId -> userId.equals(String.valueOf(toUser.getUserId())));
            // 채팅방 밖이면 알림 발송
            if (!isUserInRoom) {
                String fromUserName;
                try {
                    if(fromUser.getUserCategory() == 2) { // 보육유저면 보육원 Name으로 저장.
                        BoyugUserDto boyugUser = accountService.getBoyugUserInfo(fromUser.getUserId());
                        fromUserName = boyugUser.getBoyugUserName();
                    } else {
                        fromUserName = fromUser.getUserName();
                    }
                    sendMessage(chatRoom.getChatRoomId(), fromUser.getUserId(), fromUserName,
                                toUser.getUserId(), message2, savedNotificationId.getNotificationId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // 채팅창 안에 있으면
                notificationRepository.updateIsReadByNotificationId(savedNotificationId.getNotificationId()); // 알림 바로 읽음 처리
                chattingService.updateChatMessageIsRead(chatRoom.getChatRoomId(), toUser.getUserId()); // 상대방 채팅도 바로 읽음 처리
            }
        }
    }

    @Override
    public List<NotificationDto> getUnreadNotifications(UserDto toUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sendDate"));
        List<NotificationEntity> entities = notificationRepository.findNotificationsWithLimit(toUser.toEntity(), false, pageable);

        return entities.stream()
                .map(NotificationDto::of)
                .collect(Collectors.toList());
    }

    public void markAsRead(int notificationId) {
        notificationRepository.updateIsReadByNotificationId(notificationId);
    }

    @Override
    public void markAsReadAll(int userId) {
        notificationRepository.updateIsReadByToUserId(userId);
    }

    // 로그인 유저 SSE 서버 접속처리
    @Override
    public SseEmitter createConnection(String userId) {
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(30)); // 30분 설정

        // 사용자 저장
        emitters.put(userId, emitter);

        // 연결종료 or 시간초과시 map data 삭제
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        sendToClient(userId);

        return emitter;
    }

    // 알림 보내기
    public void sendMessage(int chatRoomId, int fromUserId, String fromUserName, int toUserId, String message, int notificationId) {
        // toUser에게 알림 발송 - 알림 메세지 저장은 따로 함
        Map<String, Object> param = new HashMap<>();
        param.put("chatRoomId", String.valueOf(chatRoomId));
        param.put("fromUserId", String.valueOf(fromUserId));
        param.put("fromUserName", fromUserName);
        param.put("toUserId", String.valueOf(toUserId));
        param.put("message", message);
        param.put("notificationId", notificationId);

        sendNotificationToUser(param);
    }

    public void sendNotificationToUser(Map<String, Object> param) {
        SseEmitter emitter = emitters.get(param.get("toUserId"));
        if (emitter != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonData = objectMapper.writeValueAsString(param);

                emitter.send(SseEmitter.event()
                        .name("chatNotification")
                        .data(jsonData));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    public static void removeEmitter(String userId) {
        emitters.remove(userId);
    }


//    *첫 연결시 더미 데이터 발송
//    처음 SSE 응답을 할 때 아무런 이벤트도 보내지 않으면 재연결 요청을 보내거나,
//    연결 요청 자체에서 오류가 발생하기 때문입니다. 따라서 첫 SSE 응답을 보낼 시
//    더미 데이터를 넣어 이러한 오류를 방지하기 위해 전송합니다.
    private void sendToClient(String userId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonData = objectMapper.writeValueAsString("연결완료");
                emitter.send(SseEmitter.event()
                        .name("chatNotification")
                        .data(jsonData));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

}
