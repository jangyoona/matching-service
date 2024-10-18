package com.boyug.dto;

import com.boyug.entity.UserSessionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSessionDto {

    private int userSessionId;

    private Date userSessionRegdate;

    private Date userSessionModifydate;

    private Boolean userSessionActive;

    private int userSessionRequest;

    private int userId;

    private UserDto user;


    public UserSessionEntity toEntity() {
        return UserSessionEntity.builder()
                .userSessionId(userSessionId)
//                .userSessionRegdate(userSessionRegdate)
//                .userSessionModifydate(userSessionModifydate)
//                .userSessionActive(userSessionActive)
//                .userSessionRequest(userSessionRequest)
                .user(user.toEntity())
                .build();
    }

    public static UserSessionDto of(UserSessionEntity entity) {
        return UserSessionDto.builder()
                .userSessionId(entity.getUserSessionId())
                .userSessionRegdate(entity.getUserSessionRegdate())
                .userSessionModifydate(entity.getUserSessionModifydate())
                .userSessionActive(entity.getUserSessionActive())
                .userSessionRequest(entity.getUserSessionRequest())
                .user(UserDto.of(entity.getUser()))
                .build();
    }
}
