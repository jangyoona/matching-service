package com.boyug.security;

import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.service.NotificationServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 기존 세션만 가져와서 삭제
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        WebUserDetails userDetails = getUserDetails(authentication);

        // Socket user 저장된 목록은 웹소켓 종료 핸들러에서 지움
        // SSE 접속 사용자 제거
        NotificationServiceImpl.removeEmitter(String.valueOf(userDetails.getUser().getUserId()));


        // 카카오 유저 로그아웃
        if(userDetails.getUser().getSocialId() != null) {
            String kakaoLogoutUrl = getKakaoLogoutUrl(request);
            response.sendRedirect(kakaoLogoutUrl);
            return;
        }

        response.sendRedirect("/home");
    }


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
