package com.boyug.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TBL_BOYUGPROGRAMDETAIL")
public class BoyugProgramDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int boyugProgramDetailId;

    @Column(nullable = false)
    private LocalDate boyugProgramDetailDate;

    @Column(nullable = false)
    private LocalTime boyugProgramDetailStartTime;

    @Column(nullable = false)
    private LocalTime boyugProgramDetailEndTime;

    @Column(nullable = false)
    private int boyugProgramDetailChild;

    @Builder.Default
    @Column
    private int programDetailPerson = 0;

    @Builder.Default
    @Column
    private boolean boyugProgramDetailActive = true;

    @Builder.Default
    @Column
    private boolean boyugProgramDetailIsOpen = true;

    @Builder.Default
    @Column
    private int boyugProgramDetailRequest = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "boyugProgramId")
    private BoyugProgramEntity boyugProgram;

    @ManyToOne
    @JoinColumn(name = "sessionId")
    private SessionEntity session;

    @OneToMany(mappedBy = "boyugProgramDetail", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserToBoyugEntity> userToBoyugs;

    @OneToMany(mappedBy = "boyugProgramDetail", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BoyugToUserEntity> boyugToUsers;

}
