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
@Table(name = "TBL_USERDETAIL")
public class UserDetailEntity {

    @Column(nullable = false)
    private Date userBirth;

    @Column(nullable = false, length = 10)
    private String userGender;

    @Column(nullable = false, length = 30)
    private String protectorPhone;

    @Column(nullable = false, length = 10)
    private String userHealth;

    @Id
    private int userId;

    @OneToOne
    @PrimaryKeyJoinColumn
    private UserEntity user;

}
