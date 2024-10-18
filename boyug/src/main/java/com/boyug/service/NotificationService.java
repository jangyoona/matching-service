package com.boyug.service;

import com.boyug.dto.ChatRoomDto;
import com.boyug.dto.NotificationDto;
import com.boyug.dto.UserDto;
import com.boyug.entity.NotificationEntity;
import com.boyug.entity.UserEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    void insertNotification(UserDto fromUser, UserDto toUser, ChatRoomDto chatRoom, String message, String message2);

    List<NotificationDto> getUnreadNotifications(UserDto toUser, int page, int size);

    void markAsRead(int notificationId);

    void markAsReadAll(int userId);

    // SSE 연결
    SseEmitter createConnection(String userId);

}
