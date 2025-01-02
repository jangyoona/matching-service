package com.boyug.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageVO {
    private int roomNumber;
    private String message;
    private int fromUserId;
    private int toUserId;
    private String chatSendTime;

}
