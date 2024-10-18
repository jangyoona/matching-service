package com.boyug.dto;

import com.boyug.entity.ProfileImageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.interfaces.PBEKey;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileImageDto {

    private int imageId;
    private UserDto user;
    private int userId;
    private String imgOriginName;
    private String imgSavedName;
    private Date savedDate = new Date();

    public ProfileImageEntity toEntity() {
        return ProfileImageEntity.builder()
                .user(user.toEntity())
                .imgOriginName(imgOriginName)
                .imgSavedName(imgSavedName)
                .build();
    }

    public static ProfileImageDto of(ProfileImageEntity entity) {
        return ProfileImageDto.builder()
                .imageId(entity.getImageId())
                .user(UserDto.of(entity.getUser()))
                .userId(entity.getUser().getUserId())
                .imgOriginName(entity.getImgOriginName())
                .imgSavedName(entity.getImgSavedName())
                .savedDate(entity.getSavedDate())
                .build();
    }

}
