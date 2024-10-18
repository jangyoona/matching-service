package com.boyug.oauth2;

import java.util.Map;

public class KakaoResponse implements OAut2Response.OAuth2Response {
    private Map<String, Object> attribute;
    private Map<String, Object> kakaoAccountAttribute;
    private Map<String, Object> profileAttribute;



    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
        this.kakaoAccountAttribute = (Map<String, Object>) attribute.get("kakao_account");
        this.profileAttribute = (Map<String, Object>) kakaoAccountAttribute.get("profile");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getName() {
        return profileAttribute.get("nickname").toString();
    }

    @Override
    public String getProfileImage() {
        return kakaoAccountAttribute.get("profile").toString();
    }


//    @Override
//    public String getEmail() {
//        return kakaoAccountAttribute.get("email").toString();
//    }


}
