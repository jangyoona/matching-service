package com.boyug.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GroupwareWebSocketHandler extends TextWebSocketHandler {

    // empId와 WebSocketSession을 매핑하는 맵
    private final Map<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // WebSocketSession의 URI에서 empId를 추출합니다.
        String empIdStr = session.getUri().getQuery().split("empId=")[1];

        // empId를 Integer로 변환하고 세션에 저장
        Integer empId = Integer.valueOf(empIdStr);
        session.getAttributes().put("empId", empId);
        sessions.put(empId, session); // empId와 세션을 매핑하여 저장합니다.
    }




    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 세션을 찾기 위해 empId를 이용하여 제거합니다.
        String empIdStr = session.getUri().getQuery().split("empId=")[1];

        // empId를 Integer로 변환하고 세션에 저장
        Integer empId = Integer.valueOf(empIdStr);
        session.getAttributes().put("empId", empId);
        sessions.remove(empId); // empId에 해당하는 세션 제거

    }

    public void sendMessageToAll(String message) throws IOException {
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    public void sendMessageToSomeone(String message, int empId) throws IOException {
        WebSocketSession session = sessions.get(empId); // empId에 해당하는 세션 가져오기
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }
}
