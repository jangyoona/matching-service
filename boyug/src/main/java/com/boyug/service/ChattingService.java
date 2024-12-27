package com.boyug.service;

import com.boyug.dto.ChatMessageDto;
import com.boyug.dto.ChatRoomDto;
import com.boyug.dto.UserDto;
import com.boyug.security.WebUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

public interface ChattingService {
    int getNextChatRoomId();

    List<ChatRoomDto> createOrGetChatRoom(WebUserDetails userDetails, int toUserId);

    ChatRoomDto getChatRoom(int roomNumber);

    ChatRoomDto getByIdWithMessagesSorted(int chatRoomId);

    void insertChatMessage(ChatMessageDto sendMessage);

    void sendMessage(WebUserDetails userDetails, String roomNumber, String message, int toUserId);

    ModelAndView prepareChatView(int roomNumber, WebUserDetails userDetails, HttpServletRequest request);

    List<ChatMessageDto> getMessagesByRoomId(int roomNumber);

    void updateChatMessageIsRead(int chatRoomId, int userId);

    List<ChatRoomDto> getChatRoomByUserId(UserDto loginUser);

    void editChatRoomActive(WebUserDetails user, int[] selectedValues);
}
