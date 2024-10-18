package com.boyug.service;

import com.boyug.dto.ChatMessageDto;
import com.boyug.dto.ChatRoomDto;
import com.boyug.dto.UserDto;
import com.boyug.entity.ChatMessageEntity;
import com.boyug.entity.ChatRoomEntity;
import com.boyug.repository.ChatMessageRepository;
import com.boyug.repository.ChatRoomRepository;
import com.boyug.security.WebUserDetails;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public class ChattingServiceImpl implements ChattingService {

    @Setter
    ChatRoomRepository chatRoomRepository;

    @Setter
    ChatMessageRepository chatMessageRepository;


    @Override
    public int getNextChatRoomId() {
        return chatRoomRepository.findNextChatRoomId();
    }

    @Override
    public int insetChatRoom(ChatRoomDto room) {
        ChatRoomEntity entity = room.toEntity();
        ChatRoomEntity savedEntity = chatRoomRepository.save(entity);
        return savedEntity.getChatRoomId();
    }

    @Override
    public Integer getRoomDupCheck(UserDto loginUser, int otherUserId) {
        Integer isChatRoom = 0;
        if(loginUser.getUserCategory() == 2) {
            isChatRoom = chatMessageRepository.findChatRoomByUserIdsAndBoyugType(loginUser.getUserId(), otherUserId);
        } else if(loginUser.getUserCategory() == 3) {
            isChatRoom = chatMessageRepository.findChatRoomByUserIdsAndUserType(loginUser.getUserId(), otherUserId);
        }

        return isChatRoom;
    }

    @Override
    public ChatRoomDto getChatRoom(int roomNumber) {
        Optional<ChatRoomEntity> entity = chatRoomRepository.findById(roomNumber);
//        return entity.isPresent() ? ChatRoomDto.of(entity.get()) : null;
        return entity.map(ChatRoomDto::of).orElse(null);
    }

    @Override
    public void insertChatMessage(ChatMessageDto sendMessage) {
        ChatMessageEntity entity = sendMessage.toEntity();
        chatMessageRepository.save(entity);

//        // 알림 전송
//        sendMessage(sendMessage.getFromUserId().getUserId(), sendMessage.getFromUserId().getUserName(),
//                    sendMessage.getToUserId().getUserId(), sendMessage.getChatContent());
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
