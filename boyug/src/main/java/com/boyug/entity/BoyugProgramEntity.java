package com.boyug.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TBL_BOYUGPROGRAM")
public class BoyugProgramEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int boyugProgramId;

    @Column(nullable = false, length = 50)
    private String boyugProgramName;

    @Column(nullable = false, length = 500)
    private String boyugProgramDesc;

    @Builder.Default
    @Column
    private Date boyugProgramRegdate = new Date();

    @Builder.Default
    @Column
    private Date boyugProgramModifydate = new Date();

    @Builder.Default
    @Column
    private boolean boyugProgramActive = true;

    @Builder.Default
    @Column
    private int boyugProgramCount = 0;

    @Builder.Default
    @Column
    private boolean boyugProgramIsOpen = true;

    // BoyugProgramDetail 테이블과 해당 테이블 사이의 1 : Many 관계를 구현하는 필드
    @OneToMany(mappedBy = "boyugProgram", fetch = FetchType.LAZY, cascade = CascadeType.ALL)  // 지워지면 자식 테이블은 어떻게 할건지 설정
    private List<BoyugProgramDetailEntity> programDetails;

    // BoyugUserEntity 테이블과 해당 테이블 사이의 1 : Many 관계를 구현하는 필드
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "userId")
    private BoyugUserEntity boyugUser;

}
