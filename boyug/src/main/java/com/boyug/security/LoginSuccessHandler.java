package com.boyug.security;

import com.boyug.repository.AccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;


public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Setter(onMethod_ = { @Autowired})
    private AccountRepository accountRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        WebUserDetails userDetails = (WebUserDetails) authentication.getPrincipal();

        // 로그인 유저가 보육 회원이고, 관리자 승인을 받기 전 일 때 pending -> logout redirect
        if(userDetails.getUser().getUserCategory() == 2) {
            boolean isConfirm = false;
            try {
                isConfirm = accountRepository.boyugUserIsConfirm(authentication.getName());
            } catch (Exception e) {
                System.out.println("없는 유저: " + e.getMessage());
            }
            if(!isConfirm) {
                response.sendRedirect("/userView/account/pending");
                return;
            }
        }

        if (userDetails.getUser().getUserCategory() == 3) {
            response.sendRedirect("/personal/personalHome");
            return;
        }

        // 아이디 저장 설정
        String saveId = request.getParameter("saveId"); // true or false
        String username = authentication.getName();
        if (username != null) {
            // 기존 쿠키 있으면 삭제
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("saveId")) {
                        // 기존 쿠키가 있으면 삭제
                        Cookie deleteCookie = new Cookie("saveId", "");
                        deleteCookie.setMaxAge(0);
                        deleteCookie.setPath("/");
                        response.addCookie(deleteCookie);
                        break;
                    }
                }
            }

            // 아이디 저장 체크하면 쿠키 새로 저장 or 미 체크시 새로 저장안하고 위에서 삭제만 됨.
            if (saveId != null) {
                Cookie newCookie = new Cookie("saveId", username);
                newCookie.setMaxAge(60 * 60 * 24 * 30); // 30일 저장
                newCookie.setPath("/");
                response.addCookie(newCookie);
            }
        }
        response.sendRedirect("/home");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {

    }
}
