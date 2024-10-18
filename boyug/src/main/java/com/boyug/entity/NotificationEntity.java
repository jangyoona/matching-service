package com.boyug.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "TBL_NOTIFICATION")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int notificationId;

    @ManyToOne
    @JoinColumn(name = "fromUser")
    private UserEntity fromUser; // 알림 보내는 사람

    @ManyToOne
    @JoinColumn(name = "toUser")
    private UserEntity toUser; // 알림을 받는 사람

    @Column(length = 250)
    @NotNull
    private String message;

    @ManyToOne
    @JoinColumn(name = "chatRoomId")
    private ChatRoomEntity chatRoom; // 해당 알림의 채팅 정보

    @Column
    @CreationTimestamp
    private Timestamp sendDate = new Timestamp(System.currentTimeMillis());

    @Column
    @Builder.Default
    private boolean isRead = false;

    @Column
    @Builder.Default
    private boolean active = true;


}
