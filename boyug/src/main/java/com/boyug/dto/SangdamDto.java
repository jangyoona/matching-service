package com.boyug.dto;

import com.boyug.entity.SangdamEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SangdamDto {

    private int sangdamId;

    private UserDto user;

    private String sangdamContent;

    private String sangdamActive;

    public SangdamEntity toEntity() {
        return SangdamEntity.builder()
                .sangdamId(sangdamId)
                .sangdamContent(sangdamContent)
//                .sangdamActive(sangdamActive)
                .build();
    }
    public static SangdamDto of(SangdamEntity entity) {
        return SangdamDto.builder()
                .sangdamId(entity.getSangdamId())
                .user(UserDto.of(entity.getUser()))
                .sangdamContent(entity.getSangdamContent())
                .sangdamActive(entity.getSangdamActive())
                .build();
    }
}
