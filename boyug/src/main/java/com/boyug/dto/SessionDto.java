package com.boyug.dto;

import java.util.Date;

import com.boyug.entity.SessionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDto {

    private int sessionId;
    private String sessionName;
    private Boolean sessionActive;

    // DTO를 엔티티로 변환
    public SessionEntity toEntity() {
        return SessionEntity.builder()
                .sessionId(sessionId)
                .sessionName(sessionName)
                .sessionActive(sessionActive)
                .build();
    }

    // 엔티티를 DTO로 변환
    public static SessionDto of(SessionEntity entity) {
        return SessionDto.builder()
                .sessionId(entity.getSessionId())
                .sessionName(entity.getSessionName())
                .sessionActive(entity.getSessionActive())
                .build();
    }

}
