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
@Table(name = "TBL_BOARD")
public class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int boardId;

    @Column(nullable = false, length = 50)
    private String boardTitle;

    @Column(nullable = false, length = 1000)
    private String boardContent;
    @Column(nullable = false)
    private String writer;

    @Builder.Default
    @Column
    private Date boardRegdate = new Date();

    @Builder.Default
    @Column
    private Date boardModifydate = new Date();

    @Column
    private int boardCount;

    @Builder.Default
    @Column
    private Boolean boardAnswer = false;

    @Builder.Default
    @Column
    private int boardCategory = 1;

    @Builder.Default
    @Column
    private Boolean boardActive = true;



    @ManyToOne
    @JoinColumn(name = "userId")
    private UserEntity user;

    // board 테이블과 boardattach 테이블 사이의 1 : Many 관계를 구현하는 필드
    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BoardAttachEntity> attachments;

}
