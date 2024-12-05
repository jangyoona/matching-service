package com.boyug.security.jwt;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtInfoDto {

    private String grantType;

    private String accessToken;

    private long accessTokenExpireTime;

    private String refreshToken;

    private long refreshTokenExpireTime;

}
