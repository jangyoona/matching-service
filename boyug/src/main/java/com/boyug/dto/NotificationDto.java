package com.boyug.dto;

import com.boyug.entity.ChatRoomEntity;
import com.boyug.entity.NotificationEntity;
import com.boyug.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {

    private int notificationId;
    private UserDto fromUser;
    private UserDto toUser;
    private String message;
    private ChatRoomDto chatRoom;
    private Timestamp sendDate;
    private boolean isRead;
    private boolean active;

    public NotificationEntity toEntity() {
        return NotificationEntity.builder()
                .notificationId(notificationId)
                .fromUser(fromUser.toEntity())
                .toUser(toUser.toEntity())
                .message(message)
                .chatRoom(chatRoom.toEntity())
//                .sendDate(sendDate)
//                .isRead(isRead)
//                .active(active)
                .build();
    }

    public static NotificationDto of(NotificationEntity entity) {
        return NotificationDto.builder()
                .notificationId(entity.getNotificationId())
                .fromUser(UserDto.of(entity.getFromUser()))
                .toUser(UserDto.of(entity.getToUser()))
                .message(entity.getMessage())
                .chatRoom(ChatRoomDto.of(entity.getChatRoom()))
                .sendDate(entity.getSendDate())
                .isRead(entity.isRead())
                .active(entity.isActive())
                .build();
    }
}
