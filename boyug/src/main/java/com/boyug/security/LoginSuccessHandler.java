package com.boyug.security;

import com.boyug.repository.AccountRepository;
import com.boyug.security.jwt.JwtInfoDto;
import com.boyug.security.jwt.JwtUtil;
import com.boyug.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;

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

        // 아이디 저장 설정
        saveId(request, response, authentication);

        // Jwt Token 발급 and 자동로그인 체크 유무에 따른 Refresh Token 기간 설정
        AddAuthenticationJwtToken(userDetails, request, response);

        // 로그인 유저 아이디 세션에 저장
        HttpSession session = request.getSession();
        session.setAttribute("loginUserId", userDetails.getUser().getUserId());

        // 개인회원 Home 이동
        if (userDetails.getUser().getUserCategory() == 3) {
            response.sendRedirect("/personal/personalHome");
            return;
        }

        // 업체회원 Home 이동
        response.sendRedirect("/home");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {

    }

    /**
     * JWT 토큰 발급 + 쿠키 저장
     **/
    public void AddAuthenticationJwtToken(WebUserDetails userDetails, HttpServletRequest request, HttpServletResponse response) {
        String rememberMe = request.getParameter("remember-me"); // on or null
        JwtInfoDto token = jwtUtil.createToken(userDetails, rememberMe);
//        response.setHeader("Authorization", "Bearer " + token); // 이거 못 읽어서 아래 4줄로 (localStorage)

//        System.out.println("success Handler token response ==========>> " + token);
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//        response.getWriter().write("{\"Authorization\": \"" + token + "\"}");

        // 토큰 cookie 저장
        String encodedToken = null;
        String encodedToken_Refresh = null;

        try {
            encodedToken = URLEncoder.encode(token.getAccessToken(), StandardCharsets.UTF_8.toString());
            encodedToken_Refresh = URLEncoder.encode(token.getRefreshToken(), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            System.out.println("토큰 인코딩 오류");
            throw new RuntimeException(e);
        }


        // Token 저장
        Cookie jwtCookie = new Cookie("Authorization", encodedToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 30); // 30분
        response.addCookie(jwtCookie);

        // RefreshToken 저장
        int refreshTokenExpTime = 60 * 60 * 24 * 7; // 7일
        if (rememberMe != null) {
            refreshTokenExpTime = 60 * 60 * 24 * 30; // 30일
        }

        Cookie jwtRefreshCookie = new Cookie("RefreshToken", encodedToken_Refresh);
        jwtRefreshCookie.setHttpOnly(true);
        jwtRefreshCookie.setPath("/");
        jwtRefreshCookie.setMaxAge(refreshTokenExpTime);
        response.addCookie(jwtRefreshCookie);

        redisService.addJwtRefreshToken(userDetails.getUser().getUserId(), encodedToken_Refresh);
    }

    /**
     * 아이디 저장
     **/
    public void saveId(HttpServletRequest request, HttpServletResponse response,Authentication authentication) {
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
    }


}
