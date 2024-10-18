package com.boyug.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "TBL_BOYUGUSER")
public class BoyugUserEntity {

    @Column(nullable = false)
    private String boyugUserName;

    @Column(nullable = false)
    private String boyugUserEmail;

    @Column(nullable = false)
    private int boyugUserChildNum;

    @Builder.Default
    @Column
    private boolean boyugUserConfirm = false;

    @Id
    private int userId;

    @OneToOne
    @PrimaryKeyJoinColumn
    private UserEntity user;

    @OneToMany(mappedBy = "boyugUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BoyugUserFileEntity> boyugUserFiles;

}
