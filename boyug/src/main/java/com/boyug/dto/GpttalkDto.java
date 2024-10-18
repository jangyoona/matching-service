package com.boyug.dto;

import com.boyug.entity.GptTalkEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpttalkDto {

    private int gptId;
    private int userId;

//    public GptTalkEntity toEntity() {
//        GptTalkEntity gpttalkEntity = GptTalkEntity.builder()   .userId(userId)
//                                                                .build();
//        return gpttalkEntity;
//    }
//
//    public GpttalkDto of(GptTalkEntity entity) {
//        GpttalkDto gpttalkDto = GpttalkDto.builder()    .gptId(entity.getGptId())
//                                                        .userId(entity.getUserId())
//                                                        .build();
//        return  gpttalkDto;
//    }

}
