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
@Table(name = "TBL_USERSESSION")
public class UserSessionEntity {

    @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int userSessionId;

    @Builder.Default
    @Column
    private Date userSessionRegdate = new Date();

    @Builder.Default
    @Column
    private Date userSessionModifydate = new Date();

    @Builder.Default
    @Column
    private Boolean userSessionActive = true;

    @Builder.Default
    @Column
    private int userSessionRequest = 0;

//    @Column
//    private int userId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private UserEntity user;
}
