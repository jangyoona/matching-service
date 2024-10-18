package com.boyug.dto;

import com.boyug.entity.BoyugUserEntity;
import com.boyug.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoyugUserDto {

    private int userId;
    private String boyugUserName;
    private String boyugEmail;
    private int boyugChildNum;
    private boolean boyugUserConfirm;

    private UserEntity user;

    private List<BoyugUserFileDto> boyugUserFiles; // 보육유저파일 리스트

    public BoyugUserEntity toEntity() {
        return BoyugUserEntity.builder()
                .userId(userId)
                .boyugUserName(boyugUserName)
                .boyugUserEmail(boyugEmail)
                .boyugUserChildNum(boyugChildNum)
//                .boyugUserConfirm(boyugUserConfirm)
                .build();
    }

    public static BoyugUserDto of(BoyugUserEntity entity, UserEntity userEntity) {
        BoyugUserDto dto = new BoyugUserDto();
        return BoyugUserDto.builder()
                .userId(entity.getUserId())
                .boyugUserName(entity.getBoyugUserName())
                .boyugEmail(entity.getBoyugUserEmail())
                .boyugChildNum(entity.getBoyugUserChildNum())
                .boyugUserConfirm(entity.isBoyugUserConfirm())
                .user(userEntity)
                // 보육유저파일 리스트를 DTO로 변환
                .boyugUserFiles(entity.getBoyugUserFiles().stream()
                        .map(BoyugUserFileDto::of)
                        .collect(Collectors.toList()))
                .build();
    }

    public static BoyugUserDto of(BoyugUserEntity entity) {
        BoyugUserDto dto = new BoyugUserDto();
        return BoyugUserDto.builder()
                .userId(entity.getUserId())
                .boyugUserName(entity.getBoyugUserName())
                .boyugEmail(entity.getBoyugUserEmail())
                .boyugChildNum(entity.getBoyugUserChildNum())
                .boyugUserConfirm(entity.isBoyugUserConfirm())
                .build();
    }
}