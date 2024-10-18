package com.boyug.security;

import com.boyug.controller.ChattingController;
import com.boyug.dto.ChatRoomDto;
import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.service.NotificationServiceImpl;
import com.boyug.websocket.SocketHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class LogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {

    private Map<String, List<WebSocketSession>> sessionMap; // WebSocket 세션을 저장하는 Map

    public LogoutSuccessHandler() {}
    public LogoutSuccessHandler(Map<String, List<WebSocketSession>> sessionMap) {
        this.sessionMap = sessionMap;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        WebUserDetails userDetails = null;
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

            // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
            if (authentication instanceof OAuth2AuthenticationToken) {
                userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
            } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                userDetails = (WebUserDetails) authentication.getPrincipal();
            }
        }

        if(userDetails == null) {
            response.sendRedirect("/home");
            return;
        }

        String userId = String.valueOf(userDetails.getUser().getUserId());

        // Socket user 저장된 목록 다시 확인 후 지우기
        List<String> users = SocketHandler.getUsers();
        if (!users.isEmpty()) {
            users.removeIf(user -> user.equals(userId));
        }
        // SSE 접속 사용자 제거
        NotificationServiceImpl.removeEmitter(userId);

        response.sendRedirect("/home");
    }
}
