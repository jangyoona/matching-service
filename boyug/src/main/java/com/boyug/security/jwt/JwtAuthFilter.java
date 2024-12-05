package com.boyug.security.jwt;

import com.boyug.security.WebUserDetails;
import com.boyug.security.WebUserDetailsService;
import com.boyug.service.AccountService;
import com.boyug.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Set;


@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final WebUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final AccountService accountService;

    public JwtAuthFilter(@Lazy WebUserDetailsService userDetailsService, JwtUtil jwtUtil, RedisService redisService, AccountService accountService) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
        this.accountService = accountService;
    }

    // 필터 제외할 경로 설정
    private static final Set<String> EXCLUDED_PATHS =
            Set.of("/userAssets/", "/profile-image/", "/img/", "/notifications/",
                    "/userView/account/login", "/userView/account/socialId-check", "/userView/account/process-login",
                    "/userView/account/business-register", "/personal/personal-register",
                    "/userView/account/success", "/userView/account/form", "/userView/account/reset-passwd");



    /**
     * JWT 토큰 검증 필터 수행
     **/
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 필터에서 제외할 경로
        String path = request.getRequestURI();
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 쿠키에서 JWT 추출
        String token = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            // Jwt Access Token 유효성 검증
            if (jwtUtil.validateToken(token, "access")) {

                // 토큰에서 유저 Phone 추출 후 userDetails 생성
                WebUserDetails userDetails = extractUserDetailsFromToken(token, "access");

                if (userDetails != null) {
                    // UserDetsils, Password, Role -> 접근권한 인증 Token 생성
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    //현재 Request의 Security Context에 접근권한 설정
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } else {
                removeAccessToken(response);
            }
        } else {
            // Access Token 만료시 Refresh Token 확인
            String userRefreshToken = getRefreshToken(request);
            if (userRefreshToken != null) {
                String refreshToken = redisService.getJwtRefreshTokenToUserId(jwtUtil.getUserId(userRefreshToken, "refresh"));

                if (refreshToken != null && refreshToken.equals(userRefreshToken) && jwtUtil.validateToken(userRefreshToken, "refresh")) {
                    // Refresh Token 유효성 검사 통과시 Access Token 재발급
                    // 토큰에서 유저Id 추출 후 userDetails 생성
                    WebUserDetails userDetails = extractUserDetailsFromToken(userRefreshToken, "refresh");
                    if (userDetails != null) {
                        // Access Token 재발급
                        newAccessToken(userDetails, response);

                        // Refresh Token 유효기간 확인 후 1일 이하일 경우 재발급
                        ZonedDateTime refreshTokenExpiration = ZonedDateTime.parse(jwtUtil.getExpiration(userRefreshToken, "refresh"));
                        ZonedDateTime oneDayFromNow = ZonedDateTime.now().plusDays(1L);
                        if (refreshTokenExpiration.isBefore(oneDayFromNow)) {
                            // 기존 Refresh Token 삭제 + New Refresh Token 저장
                            redisService.addJwtRefreshToken(userDetails.getUser().getUserId(), newRefreshToken(userDetails, response));
                        }

                        // UserDetsils, Password, Role -> 접근권한 인증 Token 생성
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        //현재 Request의 Security Context에 접근권한 설정
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } else {
                    // Refresh Token 유효성 검사 실패 시 RefreshToken + Redis 모두 삭제 (로그아웃)
                    removeRefreshToken(response);
                    redisService.removeJwtRefreshTokenToUserId(jwtUtil.getUserId(userRefreshToken, "refresh"));
                }
            }
        }
        filterChain.doFilter(request, response); // 다음 필터로 넘기기
    }


    /**
     * Token 에서 추출한 userPhone 으로 userDetails 생성
     **/
    public WebUserDetails extractUserDetailsFromToken(String token, String type) {
        String userPhone = accountService.getUserPhone(jwtUtil.getUserId(token, type));
//        String userPhone = jwtUtil.getUserPhone(token, type);
        return (WebUserDetails) userDetailsService.loadUserByUsername(userPhone);
    }

    /**
     * Access Token 쿠키에서 삭제
     **/
    private void removeAccessToken(HttpServletResponse response) {

        Cookie accessCookie = new Cookie("Authorization", null);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge(0); // 즉시 만료
        response.addCookie(accessCookie);
    }

    private void removeRefreshToken(HttpServletResponse response) {

        Cookie refreshCookie = new Cookie("RefreshToken", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        response.addCookie(refreshCookie);
    }

    /**
     * Refresh Token 쿠키에서 조회
     **/
    private String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("RefreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        return refreshToken;
    }

    /**
     * Access Token 재발급
     **/
    private void newAccessToken(WebUserDetails userDetails, HttpServletResponse response) {

        // AccessToken 재발급
        String accessToken = jwtUtil.createAccessToken(userDetails);
        String encodedToken = null;

        try {
            encodedToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            System.out.println("토큰 인코딩 오류");
            throw new RuntimeException(e);
        }

        Cookie jwtCookie = new Cookie("Authorization", encodedToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 30); // 30분
        response.addCookie(jwtCookie);
    }

    /**
     * Refresh Token 재발급 (유효기간 1일 이하인 경우)
     **/
    private String newRefreshToken(WebUserDetails userDetails, HttpServletResponse response) {

        // Refresh Token 재발급
        String encodedToken = null;

        try {
            encodedToken = URLEncoder.encode(jwtUtil.createRefreshToken(userDetails, null), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            System.out.println("토큰 인코딩 오류");
            throw new RuntimeException(e);
        }

        // New refresh Token 저장
        Cookie jwtCookie = new Cookie("RefreshToken", encodedToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60 * 24 * 7); // 7일
        response.addCookie(jwtCookie);

        return encodedToken;
    }



}

// JWT가 헤더에 있는 경우
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//            token = authorizationHeader.substring(7); // "Bearer " 자르기


// 테스트
//                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//                        if (authentication != null && authentication.isAuthenticated()) {
//                            System.out.println("인증된 사용자: " + authentication.getName());
//                        } else {
//                            System.out.println("인증되지 않은 사용자");
//                        }

