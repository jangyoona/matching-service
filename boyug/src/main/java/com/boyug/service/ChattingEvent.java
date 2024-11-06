package com.boyug.service;

import com.boyug.dto.ChatRoomDto;
import com.boyug.dto.UserDto;
import lombok.Getter;

@Getter
public class ChattingEvent {

    private final UserDto fromUser;
    private final UserDto toUser;
    private final ChatRoomDto chatRoom;
    private final String notificationMessage;
    private final String message2;

    public ChattingEvent(UserDto fromUser, UserDto toUser, ChatRoomDto chatRoom, String notificationMessage, String message2) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.chatRoom = chatRoom;
        this.notificationMessage = notificationMessage;
        this.message2 = message2;
    }
}
