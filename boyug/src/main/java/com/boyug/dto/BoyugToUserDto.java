package com.boyug.dto;

import com.boyug.entity.BoyugToUserEntity;
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
public class BoyugToUserDto {

    private int boyugToUserId;

    private int boyugProgramDetailId;

    private int userSessionId;

    private String requestIsok;

    private int userScore;

    private int boyugScore;

    private Date requestDate;

    public BoyugToUserEntity toEntity() {
        return BoyugToUserEntity.builder()
//                .boyugToUserId(boyugToUserId)
//                .requestIsOk(requestIsok)
                .build();
    }

    public static BoyugToUserDto of(BoyugToUserEntity boyugToUserEntity) {
        return BoyugToUserDto.builder()
                .boyugProgramDetailId(boyugToUserEntity.getBoyugProgramDetail().getBoyugProgramDetailId())
                .boyugToUserId(boyugToUserEntity.getBoyugToUserId())
                .requestIsok(boyugToUserEntity.getRequestIsOk())
                .userSessionId(boyugToUserEntity.getUserSession().getUserSessionId())
                .boyugScore(boyugToUserEntity.getBoyugScore() != null ? boyugToUserEntity.getBoyugScore() : 0)
                .userScore(boyugToUserEntity.getUserScore() != null ? boyugToUserEntity.getUserScore() : 0)
                .build();
    }
}
