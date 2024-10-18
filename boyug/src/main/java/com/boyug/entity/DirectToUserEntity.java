package com.boyug.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TBL_DIRECTTOUSER")
public class DirectToUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int boyugToPrivateId;

    @Builder.Default
    @Column
    private Date requestDate = new Date();

    @ManyToOne
    @JoinColumn(name = "userId")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "userSessionId")
    private UserSessionEntity userSession;

}
