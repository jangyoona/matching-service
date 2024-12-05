package com.boyug.security.jwt;

import com.boyug.security.WebUserDetailsService;
import com.boyug.service.AccountService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final WebUserDetailsService userDetailsService;
    private final AccountService accountService;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil, @Lazy WebUserDetailsService userDetailsService, AccountService accountService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.accountService  = accountService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // 쿠키에서 JWT 추출
        String token = null;

        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        HttpServletRequest req = servletRequest.getServletRequest();
        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        // JWT가 헤더에 있는 경우
        if (token != null) { // 쿠키용
            // JWT 유효성 검증
            if (jwtUtil.validateToken(token, "access")) {

                // 유저와 토큰 일치 시 userDetails 생성
                String userPhone = accountService.getUserPhone(jwtUtil.getUserId(token, "access"));
                UserDetails userDetails = userDetailsService.loadUserByUsername(userPhone);

                if (userDetails != null) {
                    // UserDetsils, Password, Role -> 접근권한 인증 Token 생성
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    //현재 Request의 Security Context에 접근권한 설정
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
