package com.boyug.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler  {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        HttpSession session = request.getSession();

        if (exception.getMessage().contains("회원가입")) {
            if (session.getAttribute("oAuth2User") != null) {
                response.sendRedirect("/personal/personal-register");
            } else {
                // 일반 로그인 성공 시 처리
                response.sendRedirect("/home");
            }
        } else {
            response.sendRedirect("/login?error");
        }
    }
}
