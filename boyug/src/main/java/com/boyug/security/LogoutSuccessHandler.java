package com.boyug.security;

import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.jwt.JwtUtil;
import com.boyug.service.NotificationServiceImpl;
import com.boyug.service.RedisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {

    private final String kakaoClientId;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    public LogoutSuccessHandler(@Value("${spring.security.oauth2.client.registration.kakao.client-id}") String kakaoClientId,
                                JwtUtil jwtUtil, RedisService redisService) {
        this.kakaoClientId = kakaoClientId;
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 기존 세션만 가져와서 삭제
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // JWT Token, 유저 정보 추출
        String accessToken = getToken(request);
        String refreshToken = getRefreshToken(request);

        if (accessToken != null || refreshToken != null) {
            String type = (accessToken != null) ? "access" : "refresh";
            String resultToken = (accessToken != null) ? accessToken : refreshToken;

            String userSocialId = jwtUtil.getSocialId(resultToken, type);
            int userId = jwtUtil.getUserId(resultToken, type);

            // JWT Token 전체 삭제
            removeAllToken(request, response);

            // JWT Refresh Token Redis 제거
            redisService.removeJwtRefreshTokenToUserId(userId);

//        WebUserDetails userDetails = getUserDetails(authentication); // 세션일 때

            // SSE 접속 사용자 제거
            NotificationServiceImpl.removeEmitter(String.valueOf(userId));

            // 카카오 유저 로그아웃
            if(userSocialId != null) {
                String kakaoLogoutUrl = getKakaoLogoutUrl(request);
                response.sendRedirect(kakaoLogoutUrl);
                return;
            }
        }

        response.sendRedirect("/home");
    }


    /**
     * kakao logout url 동적 생성
     **/
    @NotNull
    private String getKakaoLogoutUrl(HttpServletRequest request) {
//        String scheme = request.getScheme(); // "http" or "https"
        String serverName = request.getServerName(); // 도메인 이름
        int port = request.getServerPort(); // 포트 번호

        // 기본 포트번호 (80, 443) 생략 + 포트 있으면 append 없으면 삭제
        String fullDomain = serverName + ((port == 80 || port == 443) ? "" : ":" + port);

        String kakaoLogoutUrl = "https://kauth.kakao.com/oauth/logout?client_id=" + kakaoClientId +
                                "&logout_redirect_uri=http://" + fullDomain + "/";

        return kakaoLogoutUrl;
    }


    /**
     * Access Token 추출
     **/
    private String getToken(HttpServletRequest request) {
        String token = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }
        return token;
    }

    /**
     * Refresh Token 추출
     **/
    private String getRefreshToken(HttpServletRequest request) {
        String token = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("RefreshToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }
        return token;
    }
    
    /**
     * 토큰 삭제
     **/
    private void removeAllToken(HttpServletRequest request, HttpServletResponse response) {

        Cookie accessCookie = new Cookie("Authorization", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("RefreshToken", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        response.addCookie(refreshCookie);

    }

    /**
     * 세션방식일 때 유저 정보 추출
     **/
    private WebUserDetails getUserDetails(Authentication authentication) {
        WebUserDetails userDetails = null;
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

            // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
            if (authentication instanceof OAuth2AuthenticationToken) {
                userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
            } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                userDetails = (WebUserDetails) authentication.getPrincipal();
            }
        }
        return userDetails;
    }
}
