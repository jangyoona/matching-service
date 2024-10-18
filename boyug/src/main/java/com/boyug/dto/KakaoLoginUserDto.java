package com.boyug.dto;

import lombok.Data;
import lombok.Getter;

@Getter
public class KakaoLoginUserDto {

    private String id;
    private String regiDate;
    private String nickName;
    private String profileUri;

    public KakaoLoginUserDto(String id, String regiDate, String nickName, String profileUri) {
        this.id = id;
        this.regiDate = regiDate;
        this.nickName = nickName;
        this.profileUri = profileUri;
    }
}
