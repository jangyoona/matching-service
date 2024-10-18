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
@Table(name = "TBL_BOYUGTOUSER")
public class BoyugToUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int boyugToUserId;

    @Builder.Default
    @Column(length = 10)
    private String requestIsOk = "요청";

    @Column(nullable = true)
    private Integer userScore;

    @Column(nullable = true)
    private Integer boyugScore;

    @Builder.Default
    @Column
    private Date requestDate = new Date();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "boyugProgramDetailId")
    private BoyugProgramDetailEntity boyugProgramDetail;

    @ManyToOne
    @JoinColumn(name = "userSessionId")
    private UserSessionEntity userSession;

}
