package com.boyug.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    WebSocketHandler socketHandler;

    // 요청은 핸들러로 라우트 되고
    // beforeHandshake메소드에서 헤더 중 필요한 값을 가져와 true값 반환하면 Upgrade 헤더와 함께 101 Switching Protocols 상태 코드를 포함한 응답 반환
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(socketHandler, "/wss/chat")
//        registry.addHandler(socketHandler, "/ws/chating")
        registry.addHandler(socketHandler, "/chatting/{roomNumber}") // roomNumber? 방을 구분하는 값
                .addInterceptors(new HttpSessionHandshakeInterceptor(), new CustomHandshakeInterceptor())
//                .setAllowedOrigins("http://localhost:8081/chat");
                .setAllowedOrigins("http://192.168.0.15:8081//chat");
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
