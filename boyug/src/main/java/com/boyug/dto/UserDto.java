package com.boyug.dto;

import com.boyug.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private int userId;
    private String userPhone; // userPhone이 회원 아이디입니다.
    private String userPw;
    private String userName;
    private String userAddr1;
    private String userAddr2;
    private String userAddr3;
    private Date userRegDate;
    private Boolean userActive;
    private String userLatitude;
    private String userLongitude;
    private String socialId;
    private int userCategory;
    private String userType; // ROLE_ADMIN OR ROLE_BOYUG, ROLE_USER

    private Set<RoleDto> roles;

    private Set<SessionDto> favorites;

    // user 테이블과 profileimage 테이블 사이의 1 : Many 관계를 구현하는 필드
    private List<ProfileImageDto> images;

    private UserDetailDto userDetail;

    public UserEntity toEntity() {
        return UserEntity.builder()
                .userId(userId)
                .userPhone(userPhone)
                .userPw(userPw)
                .userName(userName)
                .userAddr1(userAddr1)
                .userAddr2(userAddr2)
                .userAddr3(userAddr3)
//                .userRegDate(userRegDate)
//                .userActive(userActive)
                .userLatitude(userLatitude)
                .userLongitude(userLongitude)
                .socialId(socialId != null ? socialId : null)
                .userCategory(userCategory)
                .userType(userType)
                .build();
    }

    public static UserDto of(UserEntity userEntity) {
        return UserDto.builder()
                .userId(userEntity.getUserId())
                .userPhone(userEntity.getUserPhone())
                .userPw(userEntity.getUserPw())
                .userName(userEntity.getUserName())
                .userAddr1(userEntity.getUserAddr1())
                .userAddr2(userEntity.getUserAddr2())
                .userAddr3(userEntity.getUserAddr3())
                .userRegDate(userEntity.getUserRegDate())
                .userActive(userEntity.getUserActive())
                .userLatitude(userEntity.getUserLatitude())
                .userLongitude(userEntity.getUserLongitude())
                .socialId(userEntity.getSocialId())
                .userCategory(userEntity.getUserCategory())
                .userType(userEntity.getUserType())
                // .roles(new HashSet<RoleDto>(userEntity.getRoles().stream().map(RoleDto::of).toList()))
                .build();
    }

}
