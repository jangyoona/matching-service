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
@Table(name = "TBL_BOARDATTACH")
public class BoardAttachEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int boardAttachId;

    @Column(nullable = false, length = 300)
    private String boardAttachOriginName;

    @Column(nullable = false, length = 300)
    private String boardAttachSaveName;

    @Builder.Default
    @Column
    private Date boardAttachSavedDate = new Date();

    @ManyToOne
    @JoinColumn(name = "boardId")
    private BoardEntity board;

}
