package com.boyug.dto;

import com.boyug.entity.ChatRoomEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {

    private int chatRoomId;
    private boolean boyugChatActive;
    private boolean userChatActive;

    private int roomNumber; // 임시테스트
    private String roomName; // 임시 테스트

    private int toUserId; // 임시 저장용
    private int fromUserId; // 임시 저장용

    private List<ChatMessageDto> chatMessages;

    public ChatRoomEntity toEntity() {
        return ChatRoomEntity.builder()
                .chatRoomId(chatRoomId)
//                .boyugChatActive(boyugChatActive)
//                .userChatActive(userChatActive)
                .build();
    }

    public static ChatRoomDto of(ChatRoomEntity entity) {
//        List<ChatMessageDto> messageDtos = entity.getChatMessages().stream()
//                                                 .map(ChatMessageDto::of)
//                                                 .collect(Collectors.toList());
        return ChatRoomDto.builder()
                .chatRoomId(entity.getChatRoomId())
                .boyugChatActive(entity.isBoyugChatActive())
                .userChatActive(entity.isUserChatActive())
//                .chatMessages(messageDtos)
                .chatMessages(entity.getChatMessages() != null ? entity.getChatMessages().stream()
                        .map(ChatMessageDto::of)
                        .collect(Collectors.toList()) : null)
                .build();
    }
}
