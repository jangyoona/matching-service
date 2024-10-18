package com.boyug.dto;

import com.boyug.entity.BoyugUserEntity;
import com.boyug.entity.BoyugUserFileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoyugUserFileDto {

    private int boyugUserFileId;
    private int userId;
//    private BoyugUserDto boyugUser;
    private String fileOriginName;
    private String fileSavedName;
    private Date savedDate = new Date();


    public BoyugUserFileEntity toEntity() {
        return BoyugUserFileEntity.builder()
//                .boyugUser(boyugUser.toEntity())
                .fileOriginName(fileOriginName)
                .fileSavedName(fileSavedName)
//                .savedDate(savedDate)
                .build();
    }

    public static BoyugUserFileDto of(BoyugUserFileEntity entity) {
        return BoyugUserFileDto.builder()
                .boyugUserFileId(entity.getBoyugUserFileId())
                .userId(entity.getBoyugUser().getUserId())
                .fileOriginName(entity.getFileOriginName())
                .fileSavedName(entity.getFileSavedName())
                .savedDate(entity.getSavedDate())
                .build();
    }
}
