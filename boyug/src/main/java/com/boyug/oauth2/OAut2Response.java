package com.boyug.oauth2;

public interface OAut2Response {

    public interface OAuth2Response {

        // 제공자 (kakao, naver, google, ...)
        String getProvider();

        // 제공자에서 발급해주는 아이디(번호)
        String getProviderId();

        // 사용자가 설정한 이름
        String getName();

        String getProfileImage();
    }
}
