package com.boyug.security;

import com.boyug.service.NotificationServiceImpl;
import com.boyug.websocket.SocketHandler;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionListener implements HttpSessionListener {

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {

        // 세션이 만료되었을 때 호출
        String userId = (String) event.getSession().getAttribute("loginUserId");

        // Socket user 저장된 목록 다시 확인 후 지우기
        List<String> users = SocketHandler.getUsers();
        if (!users.isEmpty()) {
            users.removeIf(user -> user.equals(userId));
        }
        // SSE 접속 사용자 제거
        NotificationServiceImpl.removeEmitter(userId);

    }

}
