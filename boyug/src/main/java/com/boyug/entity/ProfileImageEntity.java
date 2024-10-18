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
@Table(name = "TBL_PROFILEIMAGE")
public class ProfileImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int imageId;

    @Column(nullable = false, length = 300)
    private String imgOriginName;

    @Column(nullable = false, length = 300)
    private String imgSavedName;

    @Column
    @Builder.Default
    private Date savedDate = new Date();


    @ManyToOne
    @JoinColumn(name = "userId")
    private UserEntity user;
}
