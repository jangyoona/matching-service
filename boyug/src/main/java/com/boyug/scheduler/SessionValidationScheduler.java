package com.boyug.scheduler;

import com.boyug.websocket.SocketHandler;
import jakarta.servlet.http.HttpSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Component
public class SessionValidationScheduler {

    @Scheduled(fixedRate = 300000)
    public void validateSessions() {

        List<HashMap<String, Object>> sessions = SocketHandler.getRoomConnectUser();

        for (HashMap<String, Object> map : sessions) {
            for (String key : map.keySet()) {
                if (!"roomNumber".equals(key)) {
                    Object obj = map.get(key); // session.getId()로 세션 찾기
                    WebSocketSession session = (WebSocketSession) obj;
                    if (session == null) {
                        return;
                    }
                    HttpSession httpSession = (HttpSession) session.getAttributes().get("HTTP_SESSION");

                    try {
                        // HttpSession이 없거나 loginUserId 속성이 없는 경우 세션 만료로 판단
                        if (httpSession == null || httpSession.getAttribute("loginUserId") == null) {
                            session.close(new CloseStatus(4401, "Session Expired")); // WebSocket 연결 종료
                            map.remove(key);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }



}
