package com.boyug.dto;

import com.boyug.entity.BoyugProgramEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoyugProgramDto {

    private int boyugProgramId;
    private String boyugProgramName;
    private String boyugProgramDesc;
    private Date boyugProgramRegdate;
    private Date boyugProgramModifydate;
    private boolean boyugProgramActive;
    private int boyugProgramCount;
    private boolean boyugProgramIsOpen;

    private List<BoyugProgramDetailDto> programDetails;

    private UserDto user;
    private int userId;
    private String userName;
    private List<ProfileImageDto> userImg;
    private String boyugUserName;

    private String userLatitude;
    private String userLongitude;

    private double distance;
    private String formattedDistance;

    // DTO를 엔티티로 변환
    public BoyugProgramEntity toEntity() {
        return BoyugProgramEntity.builder()
                .boyugProgramId(boyugProgramId)
                .boyugProgramName(boyugProgramName)
                .boyugProgramDesc(boyugProgramDesc)
//                .boyugProgramRegdate(boyugProgramRegdate)
//                .boyugProgramModifydate(boyugProgramModifydate)
//                .boyugProgramActive(boyugProgramActive)
//                .boyugProgramCount(boyugProgramCount)
                .boyugProgramIsOpen(boyugProgramIsOpen)
                .build();
    }

    // 엔티티를 DTO로 변환
    public static BoyugProgramDto of(BoyugProgramEntity entity) {
        return BoyugProgramDto.builder()
                .boyugProgramId(entity.getBoyugProgramId())
                .boyugProgramName(entity.getBoyugProgramName())
                .boyugProgramDesc(entity.getBoyugProgramDesc())
                .boyugProgramRegdate(entity.getBoyugProgramRegdate())
                .boyugProgramModifydate(entity.getBoyugProgramModifydate())
                .boyugProgramActive(entity.isBoyugProgramActive())
                .boyugProgramCount(entity.getBoyugProgramCount())
                .boyugProgramIsOpen(entity.isBoyugProgramIsOpen())
                .userName(entity.getBoyugUser().getUser().getUserName())
                .userLatitude(entity.getBoyugUser().getUser().getUserLatitude())
                .userLongitude(entity.getBoyugUser().getUser().getUserLongitude())
                .boyugUserName(entity.getBoyugUser().getBoyugUserName())
                .userId(entity.getBoyugUser().getUser().getUserId())
                .build();
    }


}
