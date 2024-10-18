package com.boyug.dto;

import com.boyug.entity.UserDetailEntity;
import com.boyug.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailDto {

    private int userId;
    private Date userBirth;
    private String userGender;
    private String protectorPhone;
    private String userHealth;

    private UserEntity user;

    public UserDetailEntity toUserDetailEntity() {
        return UserDetailEntity.builder()
                .userId(userId)
                .userBirth(userBirth)
                .userGender(userGender)
                .protectorPhone(protectorPhone)
                .userHealth(userHealth)
                .build();
    }

    public static UserDetailDto of(UserDetailEntity userDetailEntity, UserEntity userEntity) {
        return UserDetailDto.builder()
                .userId(userDetailEntity.getUserId())
                .userBirth(userDetailEntity.getUserBirth())
                .userGender(userDetailEntity.getUserGender())
                .protectorPhone(userDetailEntity.getProtectorPhone())
                .userHealth(userDetailEntity.getUserHealth())
                .user(userEntity)
                .build();
    }

    public static UserDetailDto of(UserDetailEntity userDetailEntity) {
        return UserDetailDto.builder()
                .userId(userDetailEntity.getUserId())
                .userBirth(userDetailEntity.getUserBirth())
                .userGender(userDetailEntity.getUserGender())
                .protectorPhone(userDetailEntity.getProtectorPhone())
                .userHealth(userDetailEntity.getUserHealth())
                .user(userDetailEntity.getUser())
                .build();
    }

}
