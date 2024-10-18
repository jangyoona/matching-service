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
@IdClass(UserToBoyugId.class)
@Table(name = "TBL_USERTOBOYUG")
public class UserToBoyugEntity {
    @Id
    private int userId;

    @Id
    private int boyugProgramDetailId;

    @Builder.Default
    @Column(length = 10)
    private String requestIsOk = "신청";

    @Builder.Default
    @Column(nullable = true)
    private Integer userScore = 0;

    @Builder.Default
    @Column(nullable = true)
    private Integer boyugScore = 0;

    @Builder.Default
    @Column
    private Date requestDate = new Date();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "userId")
    private UserEntity user;

    @ManyToOne
    @MapsId("boyugProgramDetailId")
    @JoinColumn(name = "boyugProgramDetailId")
    private BoyugProgramDetailEntity boyugProgramDetail;

}
