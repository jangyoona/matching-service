package com.boyug.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TBL_CHATROOM")
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int chatRoomId;

    @Builder.Default
    @Column
    private boolean boyugChatActive = true;

    @Builder.Default
    @Column
    private boolean userChatActive = true;

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ChatMessageEntity> chatMessages;

//// 수신자 ID
//    @ManyToOne
//    @JoinColumn(name = "fromUserId") // 제약조건
//    private UserEntity toUser;
//
//// 발신자 ID
//    @ManyToOne
//    @JoinColumn(name = "toUserId") // 제약조건
//    private UserEntity fromUser;

}
