package com.boyug.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TBL_SANGDAM")
public class SangdamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int sangdamId;

//    @Column(nullable = false)
//    private int userId;

    @Column(nullable = false, length = 1000)
    private String sangdamContent;

    @Builder.Default
    @Column
    private String sangdamActive = "요청활성화";

    @ManyToOne
    @JoinColumn(name = "userId")
    private UserEntity user;

}
