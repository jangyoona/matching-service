package com.boyug.dto;

import com.boyug.entity.ChatMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

    private int chatId;
    private String chatContent;
    private Timestamp chatSendTime;

    private boolean toIsRead;

    private UserDto fromUserId;
    private UserDto toUserId;

    // 채팅방아이디
    private ChatRoomDto chatRoomId;

    // 재귀호출때문에 추가
    private int chatRoomIdTemp;

    public ChatMessageEntity toEntity() {
        return ChatMessageEntity.builder()
                .chatId(chatId)
                .chatContent(chatContent)
//                .chatSendTime(chatSendTime)
//                .fromIsRead(fromIsRead)
//                .ToIsRead(toIsRead)
                .fromUser(fromUserId.toEntity())
                .toUser(toUserId.toEntity())
                .chatRoom(chatRoomId.toEntity())
                .build();
    }

    public static ChatMessageDto of(ChatMessageEntity entity) {
        return ChatMessageDto.builder()
                .chatId(entity.getChatId())
                .chatContent(entity.getChatContent())
                .chatSendTime(entity.getChatSendTime())
                .toIsRead(entity.isToIsRead())
                .fromUserId(UserDto.of(entity.getFromUser()))
                .toUserId(UserDto.of(entity.getToUser()))
                .chatRoomIdTemp(entity.getChatRoom().getChatRoomId())
                .build();

    }
}
