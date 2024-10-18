package com.boyug.service;

import com.boyug.dto.ChatMessageDto;
import com.boyug.dto.ChatRoomDto;
import com.boyug.dto.UserDto;
import com.boyug.security.WebUserDetails;

import java.util.List;

public interface ChattingService {
    int getNextChatRoomId();

    int insetChatRoom(ChatRoomDto room);

    ChatRoomDto getChatRoom(int roomNumber);

    void insertChatMessage(ChatMessageDto sendMessage);

    List<ChatMessageDto> getMessagesByRoomId(int roomNumber);

    void updateChatMessageIsRead(int chatRoomId, int userId);

    List<ChatRoomDto> getChatRoomByUserId(UserDto loginUser);

    void editChatRoomActive(WebUserDetails user, int[] selectedValues);

    Integer getRoomDupCheck(UserDto loginUser, int otherUserId);
}
