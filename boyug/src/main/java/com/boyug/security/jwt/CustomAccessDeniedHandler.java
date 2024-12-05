package com.boyug.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j(topic = "FORBIDDEN_EXCEPTION_HANDLER")
@AllArgsConstructor
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    // 인증이 완료되었으나 접근 권한이 없는 경우

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException, ServletException {
//        log.error("No Authorities", accessDeniedException);

        log.error("No Authorities: login-denied page redirect");
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(HttpStatus.FORBIDDEN.value(), accessDeniedException.getMessage(), LocalDateTime.now());
        log.info(errorResponseDto.toString());
//        String responseBody = objectMapper.writeValueAsString(errorResponseDto);
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.setStatus(HttpStatus.FORBIDDEN.value());
//        response.setCharacterEncoding("UTF-8");
//        response.getWriter().write(responseBody);

        response.sendRedirect("/login-denied");
    }

}
