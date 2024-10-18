package com.boyug.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GptRecommendDto {

    private String recommandDesc;

    private Date recommandRegdate;

    private String recommandStatus;

}
