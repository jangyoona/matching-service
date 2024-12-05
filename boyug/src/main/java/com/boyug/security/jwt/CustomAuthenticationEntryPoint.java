package com.boyug.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j(topic = "UNAUTHORIZATION_EXCEPTION_HANDLER")
@AllArgsConstructor
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // 미인증 사용자 및 유효한 인증이 부족하여 요청이 거부된 경우
    @Getter
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException, ServletException {
//        log.error("Not Authenticated Request", authException);

        log.error("Not Authenticated Request: login page redirect");
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), authException.getMessage(), LocalDateTime.now());
        log.info(errorResponseDto.toString());

        /**
         * 아래 json으로 오류 코드(401), 오류 메세지 화면에 뿌리는데 일단 필요없어서 denied page로 이동시킴
         **/
//        ErrorResponseDto errorResponseDto = new ErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), authException.getMessage(), LocalDateTime.now());
//
//        String responseBody = objectMapper.writeValueAsString(errorResponseDto);
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
//        response.setCharacterEncoding("UTF-8");
//        response.getWriter().write(responseBody);

        response.sendRedirect("/login-denied?expiration");
    }

}
