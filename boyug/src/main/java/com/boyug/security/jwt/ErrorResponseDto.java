package com.boyug.security.jwt;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

//@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ErrorResponseDto {

    private int httpStatusCode;
    private String accessDeniedMessage;
    private LocalDateTime time;

    public ErrorResponseDto(int httpStatusValue, String accessDeniedMessage, LocalDateTime time) {
        this.httpStatusCode = httpStatusValue;
        this.accessDeniedMessage = accessDeniedMessage;
        this.time = time;
    }
}
