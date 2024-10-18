package com.boyug.dto;

import com.boyug.entity.UserToBoyugEntity;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserToBoyugDto {

    private int userId;

    private int boyugProgramDetailId;

    private String requestIsok;

    private Date requestDate;

    private int userScore;

    private int boyugScore;

    // 추가: BoyugProgramDetailDto 필드
    private BoyugProgramDetailDto boyugProgramDetailDto;

    // DTO를 엔티티로 변환
    public UserToBoyugEntity toEntity() {
        return UserToBoyugEntity.builder()
//                .requestIsOk(requestIsok)
                .userScore(userScore)
                .boyugScore(boyugScore)
                .requestDate(requestDate)
                .build();
    }

    public static UserToBoyugDto of(UserToBoyugEntity entity) {
        return UserToBoyugDto.builder()
                .userId(entity.getUser().getUserId())
                .boyugProgramDetailId(entity.getBoyugProgramDetail().getBoyugProgramDetailId())
                .requestIsok(entity.getRequestIsOk())
                //요청날짜 추가
                .requestDate(entity.getRequestDate())
                .userScore(entity.getUserScore())
                .boyugScore(entity.getBoyugScore())
                // BoyugProgramDetailDto로 변환하여 포함
                .boyugProgramDetailDto(BoyugProgramDetailDto.of(entity.getBoyugProgramDetail()))
                .build();
    }

}
