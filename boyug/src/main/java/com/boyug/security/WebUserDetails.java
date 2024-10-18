package com.boyug.security;

import com.boyug.dto.RoleDto;
import com.boyug.dto.UserDto;
import com.boyug.oauth2.CustomOAuth2User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class WebUserDetails implements UserDetails {

    @Getter
    private UserDto user;
    private List<RoleDto> roles;

    public WebUserDetails() {}
    public WebUserDetails(UserDto user) {
        this.user = user;
    }
    public WebUserDetails(UserDto user, List<RoleDto> roles) {
        this.user = user;
        this.roles = roles;
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // 권한 목록을 주는 오버라이딩 메서드

        ArrayList<SimpleGrantedAuthority> grants = new ArrayList<>();
        for (RoleDto role : roles) {
            grants.add(new SimpleGrantedAuthority(role.getRoleName()));
        }
        return grants;
    }


    @Override
    public String getPassword() {
        return user.getUserPw();
    }

    @Override
    public String getUsername() {
        return user.getUserPhone();
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
        return user.getUserActive();
    }

    // CustomOAuth2User를 WebUserDetails로 변환 메서드
    public static WebUserDetails of(CustomOAuth2User oauthUser) {
        // CustomOAuth2User에서 UserDto와 roles를 가져와 WebUserDetails 생성
        UserDto user = oauthUser.getUser();

        // RoleDto로 변환 시 roleName만 사용할 수 있으므로 기본값 설정
        List<RoleDto> roles = oauthUser.getAuthorities().stream()
                .map(auth -> RoleDto.builder()
                        .roleName(auth.getAuthority())  // roleName을 GrantedAuthority에서 설정
                        .roleDesc(auth.getAuthority())
                        .build())
                .collect(Collectors.toList());

        return new WebUserDetails(user, roles);
    }
}