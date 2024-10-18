package com.boyug.dto;

import com.boyug.entity.GptTalkDetailEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpttalkdetailDto {

    private int gptTalkDetailId;
    private int gptTalkId;
    private String gptRequest;
    private String gptResponse;
    private Date gptSendTime;

//    public GptTalkDetailEntity toEntity() {
//        GptTalkDetailEntity gpttalkdetailEntity = GptTalkDetailEntity.builder() .gptId(gptId)
//                                                                                .gptRequest(gptRequest)
//                                                                                .gptResponse(gptRsponse)
//                                                                                .gptSendtime(gptSendtime)
//                                                                                .build();
//        return  gpttalkdetailEntity;
//    }
//
//    public  static GpttalkdetailDto of (GptTalkDetailEntity entity){
//        GpttalkdetailDto gpttalkdetailDto = GpttalkdetailDto.builder()  .gptdetailId(entity.getGptdetailId())
//                                                                        .gptId(entity.getGptId())
//                                                                        .gptRequest(entity.getGptRequest())
//                                                                        .gptRsponse(entity.getGptResponse())
//                                                                        .gptSendtime(entity.getGptSendtime())
//                                                                        .build();
//        return gpttalkdetailDto;
//    }

}
