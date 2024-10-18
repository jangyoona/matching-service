package com.boyug.websocket;

import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.WebUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

public class CustomHandshakeInterceptor implements HandshakeInterceptor {

//    CustomHandshakeInterceptor : 소켓이 연결될 때 수행해야할 작업을 해주는 클래스
//
//    HandshakeInterceptor클래스를 상속받습니다.
//    웹소켓이 처음 handshake(http통신의 get방식)를 하며, 연결을 확인합니다. 이때, http요청헤더의 connection속성은 upgrade로 되어야 합니다.
//    beforeHandshake는 클라이언트의 연결요청이 들어오면 3번의 handshake에서 호출됩니다.
//    아래 코드에서는 http에 존재하는 session을 웹소켓 세션으로 등록합니다.

//    @Bean
//    public SocketHandler socketHandler() { return new SocketHandler(); }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {

        // SocketHandler 가 security 인증 정보를 바로 못 읽어서 WebSocket 세션의 속성에 저장
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 소셜로그인인 경우 변환, 아니면 WebUserDetails 직접 할당
        WebUserDetails userDetails = null;
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

            // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
            if (authentication instanceof OAuth2AuthenticationToken) {
                userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
            } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                userDetails = (WebUserDetails) authentication.getPrincipal();
            }
            attributes.put("loginUser", userDetails);
        }

        // 추가
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        HttpServletRequest req = servletRequest.getServletRequest();
        HttpSession httpSession = req.getSession();
        String sessionID = httpSession.getId();
        attributes.put("sessionID", sessionID);

        String userName = req.getParameter("userName");
        attributes.put("userPhone", userName);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
    }
}