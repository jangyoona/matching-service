package com.boyug.oauth2;

import com.boyug.dto.RoleDto;
import com.boyug.dto.UserDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class CustomOAuth2User implements OAuth2User, UserDetails {
    // 응답 정보를 담고 있는 객체
    private final OAut2Response.OAuth2Response oAuth2Response;
    @Getter
    private final UserDto user;
    private final List<RoleDto> roles;

    private final String userId; // 소셜 고유 아이디

    public CustomOAuth2User(OAut2Response.OAuth2Response oAuth2Response, String role, String userId, UserDto user, List<RoleDto> roles) {
        this.oAuth2Response = oAuth2Response;
        this.userId = userId;
        this.user = user;
        this.roles = roles;
    }

    @Override
    public Map<String, Object> getAttributes() {
//        return oAuth2Response.getAttributes();
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<SimpleGrantedAuthority> grants = new ArrayList<>();
        if (roles != null) {
            for (RoleDto role : roles) {
                grants.add(new SimpleGrantedAuthority(role.getRoleName()));
            }
        }
        return grants;
    }

    @Override
    public String getName() {
//        return oAuth2Response.getProviderId(); // 소셜 로그인 ID 반환
        return userId; // 소셜 로그인 사용자 고유 ID 반환 // 소셜 로그인 ID 반환
    }

    // UserDetails 구현 추가
    @Override
    public String getUsername() {
        return user.getUserPhone();
    }

    @Override
    public String getPassword() {
        return user.getUserPw();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getProfileImage() {
        return oAuth2Response.getProfileImage();
    }
}
