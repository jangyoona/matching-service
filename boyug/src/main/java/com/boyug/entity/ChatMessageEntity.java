package com.boyug.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TBL_CHATMESSAGE")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int chatId;

//    @CreationTimestamp
//    @Column
//    private Timestamp chatSendTime = new Timestamp(System.currentTimeMillis());

    @Column(nullable = false)
    private Timestamp chatSendTime;

    @Column(nullable = false, length = 3000)
    private String chatContent;

    @Column
    @Builder.Default
    private boolean toIsRead = false;


    // 발신자
    @ManyToOne
    @JoinColumn(name = "fromUserId")
    private UserEntity fromUser;

    // 수신자
    @ManyToOne
    @JoinColumn(name = "toUserId")
    private UserEntity toUser;

    // 채팅방
    @ManyToOne
    @JoinColumn(name = "chatRoomId")
    private ChatRoomEntity chatRoom;


//    public ChatMessageEntity(Optional<ChatRoomEntity> chatRoom, String chatContent, Timestamp chatSendTime, UserDto fromUser, UserDto toUser, boolean toIsRead) {
    public ChatMessageEntity(Optional<ChatRoomEntity> chatRoom, String chatContent, Timestamp chatSendTime, Optional<UserEntity> fromUser, Optional<UserEntity> toUser, boolean toIsRead) {
        this.chatRoom = chatRoom.get();
        this.chatContent = chatContent;
        this.chatSendTime = chatSendTime;
        this.fromUser = fromUser.get();
        this.toUser = toUser.get();
        this.toIsRead = toIsRead;
    }

}
