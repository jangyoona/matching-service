package com.boyug.websocket;

import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.WebUserDetails;
import com.boyug.service.RedisService;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {
    // https://myhappyman.tistory.com/101
    // https://micropilot.tistory.com/category/Spring%204/WebSocket%20with%20Interceptor


    // Redis Service
    @Setter
    private RedisService redisService;

    // WebSocketSession 저장
    @Getter
    static List<HashMap<String, Object>> sessionList = new CopyOnWriteArrayList<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //소켓 연결
        super.afterConnectionEstablished(session);

        String url = session.getUri().toString();
        String roomNumber = url.split("/chatting/")[1];

        // 현재 사용자 정보
        WebUserDetails userDetails = getUserInfo(session);
        String userId = String.valueOf(userDetails.getUser().getUserId());

        // 방 존재 체크 이미 있으면 세션만 추가
        boolean roomExists = false;
        for (HashMap<String, Object> existingMap : sessionList) {
            if (existingMap.get("roomNumber").equals(roomNumber)) {
                existingMap.put(session.getId(), session);
                roomExists = true;
                break;
            }
        }

        // 방이 존재하지 않으면 새로운 방 추가
        if (!roomExists) {
            HashMap<String, Object> newMap = new HashMap<>();
            newMap.put("roomNumber", roomNumber);
            newMap.put(session.getId(), session);
            sessionList.add(newMap);
        }

        // Redis 채팅방 접속자 + 세션 ID 저장
        redisService.addUserToRoomNumber(roomNumber, userId);
        redisService.addSessionToRoomNumber(roomNumber, session.getId());

        // 세션 등록이 끝나면 발급받은 세션ID 값의 메시지 발송
        JSONObject obj = new JSONObject();
        obj.put("type", "getId");
        obj.put("sessionId", session.getId());
        session.sendMessage(new TextMessage(obj.toJSONString()));
    }


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {

        // 메시지 발송
        String msg = message.getPayload();
        JSONObject obj = jsonToObjectParser(msg);
        System.out.println(obj.toJSONString());

        String rN = (String) obj.get("roomNumber");
        HashMap<String, Object> temp = new HashMap<>();

        boolean roomExists = redisService.isRoomExists(rN);
        if (roomExists) {
            for (int i = 0; i < sessionList.size(); i++) {
                String roomNumber = (String) sessionList.get(i).get("roomNumber"); // 세션리스트의 저장된 방 번호를 가져와서
                if (roomNumber.equals(rN)) { // 같은 값의 방이 존재한다면
                    temp = sessionList.get(i); // 해당 방 번호의 세션리스트의 존재하는 모든 object 값을 가져온다.
                    break;
                }
            }

            // 해당 방의 세션만 찾아서 메시지 발송
            for (String k : temp.keySet()) {
                if (k.equals("roomNumber")) { // 다만 방 번호일 경우에는 패스
                    continue;
                }

                WebSocketSession wss = (WebSocketSession) temp.get(k);
                if (wss != null && wss.isOpen()) {
                    try {
                        wss.sendMessage(new TextMessage(obj.toJSONString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 세션이 없거나 닫혀 있는 경우 알림 생성
                    try {
                        if (wss != null) {
                            wss.close(new CloseStatus(4401, "Session Expired")); // 세션 닫기
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                     temp.remove(k);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        WebUserDetails userDetails = null;
        String userId = "";

        try {
            userDetails = getUserInfo(session);
            if (userDetails != null) {
                userId = String.valueOf(userDetails.getUser().getUserId());
            }
        } catch (Exception e) {
            System.out.println("socKet connectionClosed error-----------");
            e.printStackTrace();
        } finally {
            String uri = session.getUri().toString();
            String roomNumber = extractRoomNumberFromUri(uri);

            // 채팅방 소켓 종료
            if (sessionList.size() > 0) { // 소켓이 종료되면 해당 세션값들을 찾아서 지운다.
                for (int i = 0; i < sessionList.size(); i++) {
                    HashMap<String, Object> currentMap = sessionList.get(i);
                    // 방 번호가 일치하는지 확인
                    if (currentMap.get("roomNumber").equals(roomNumber)) {
                        currentMap.remove(session.getId()); // 해당 세션만 제거
                        if (currentMap.size() == 1) { // 아이디만 남은 경우에 해당 건 제거
                            sessionList.remove(i);
                        }
                        break;
                    }
                }
            }

            boolean userInRoomNumber = redisService.isUserInRoomNumber(roomNumber, userId);

            if (userInRoomNumber) {
                if (!userId.equals("")) {
                    redisService.removeUserFromRoomNumber(roomNumber, userId);
                }
                redisService.removeSessionFromRoomNumber(roomNumber, session.getId());
            }
            super.afterConnectionClosed(session, status);
        }
    }


    // roomNumber 추출
    private String extractRoomNumberFromUri(String uri) {
        return uri.split("/chatting/")[1];  // 예: "1"
    }

    // json 파싱
    private static JSONObject jsonToObjectParser(String jsonStr) {
        JSONParser parser = new JSONParser();
        JSONObject obj = null;
        try {
            obj = (JSONObject) parser.parse(jsonStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private WebUserDetails getUserInfo(WebSocketSession session) {

        SecurityContext securityContext = (SecurityContext) session.getAttributes().get("SPRING_SECURITY_CONTEXT");
        Authentication authentication = securityContext.getAuthentication();
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