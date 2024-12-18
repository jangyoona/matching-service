package com.boyug.websocket;

import com.boyug.security.WebUserDetailsService;
import com.boyug.security.jwt.JwtHandshakeInterceptor;
import com.boyug.security.jwt.JwtUtil;
import com.boyug.service.AccountService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler socketHandler;
    private final JwtUtil jwtUtil;
    private final WebUserDetailsService userDetailsService;
    private final AccountService accountService;

    private final CustomHandshakeInterceptor customHandshakeInterceptor;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public ChatWebSocketConfig(@Lazy WebUserDetailsService userDetailsService,
                               AccountService accountService, JwtUtil jwtUtil, WebSocketHandler socketHandler,
                               CustomHandshakeInterceptor customHandshakeInterceptor, JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.socketHandler = socketHandler;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.accountService = accountService;
        this.customHandshakeInterceptor = customHandshakeInterceptor;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    // 요청은 핸들러로 라우트 되고
    // beforeHandshake메소드에서 헤더 중 필요한 값을 가져와 true값 반환하면 Upgrade 헤더와 함께 101 Switching Protocols 상태 코드를 포함한 응답 반환
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socketHandler, "/chatting/{roomNumber}") // roomNumber? 방을 구분하는 값
//                .addInterceptors(new HttpSessionHandshakeInterceptor(), new CustomHandshakeInterceptor())
                .addInterceptors(jwtHandshakeInterceptor, customHandshakeInterceptor)
                .setAllowedOrigins("http://158.247.198.164/");
    }
    // 만약 CORS때문에 origin에서 403에러가 뜬다면
    // String[] origins = {"https://www.url1.com", "https://m.url2.com", "https://url3.com"}; 배열로 여러개를 대입해도 됨

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxBinaryMessageBufferSize(500000);
        container.setMaxTextMessageBufferSize(500000);
        return container;
    }
}
