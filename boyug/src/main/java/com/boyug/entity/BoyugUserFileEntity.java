package com.boyug.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "TBL_BOYUGUSERFILE")
public class BoyugUserFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int boyugUserFileId;


    @Column(nullable = false, length = 300)
    private String fileOriginName;

    @Column(nullable = false, length = 300)
    private String fileSavedName;

    @Column
    @Builder.Default
    private Date savedDate = new Date();

    @ManyToOne
    @JoinColumn(name = "userId")
    private BoyugUserEntity boyugUser;
}
