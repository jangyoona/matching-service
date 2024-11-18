package com.boyug.security;

import com.boyug.service.NotificationServiceImpl;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

@Component
public class SessionListener implements HttpSessionListener {

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {

        HttpSession session = event.getSession();

        // 세션이 만료되었을 때 호출
        Integer userId = (Integer) session.getAttribute("loginUserId");

        // Socket user 저장된 목록은 웹소켓 종료 핸들러에서 지움
        // SSE 접속 사용자 제거
        NotificationServiceImpl.removeEmitter(String.valueOf(userId));

        // 세션 종료
        session.invalidate();


    }

}
