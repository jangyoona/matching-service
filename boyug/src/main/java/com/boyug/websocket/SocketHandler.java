package com.boyug.websocket;

import com.boyug.dto.UserDto;
import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.WebUserDetails;
import lombok.Getter;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SocketHandler extends TextWebSocketHandler {
    // https://myhappyman.tistory.com/101
    // https://micropilot.tistory.com/category/Spring%204/WebSocket%20with%20Interceptor

    static List<HashMap<String, Object>> rls = new ArrayList<>(); //웹소켓 세션을 담아둘 리스트 ---roomListSessions
    public static List<HashMap<String, Object>> getRoomConnectUser() {
        return rls;
    }

    @Getter
    static List<String> users = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //소켓 연결
        super.afterConnectionEstablished(session);
        boolean flag = false;
        String url = session.getUri().toString();
        System.out.println(url);
        String roomNumber = url.split("/chatting/")[1];

        // 현재 사용자 정보
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
        String userId = String.valueOf(userDetails.getUser().getUserId());

//        if(roomNumber.equals("1")) {
//            if (roomSessionsMap.containsKey(userId)) {
//                // 이미 연결된 경우
//                System.out.println("이미 연결된 세션입니다: " + session.getId());
//                return; // 중복 연결 방지
//            }
//        }

        int idx = rls.size(); // 방의 사이즈를 조사
        if(rls.size() > 0) {
            for(int i=0; i<rls.size(); i++) {
                String rN = (String) rls.get(i).get("roomNumber");
                if(rN.equals(roomNumber)) {
                    flag = true;
                    idx = i;
                    break;
                }
            }
        }

        if(flag) { // 존재하는 방이라면 세션만 추가한다.
            HashMap<String, Object> map = rls.get(idx);
            map.put(session.getId(), session);
            users.add(userId);
        } else { // 최초 생성하는 방이라면 방번호와 세션을 추가한다.
            if(!roomNumber.equals("1")) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("roomNumber", roomNumber);
                map.put(session.getId(), session);
                rls.add(map);
                users.add(userId);
            }
        }

        // 세션 등록이 끝나면 발급받은 세션ID 값의 메시지를 발송
        JSONObject obj = new JSONObject();
        obj.put("type", "getId");
        obj.put("sessionId", session.getId());
        session.sendMessage(new TextMessage(obj.toJSONString()));

        // 1번방 작업
//        roomSessionsMap.computeIfAbsent(roomNumber, k -> new ArrayList<>()).add(session); // 방 번호 기준으로 저장
//        roomSessionsMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(session); // 유저 아이디 기준으로 저장
    }

    // 1번방 모든 세션에 메시지 전송 함수
//    public void sendMessageToRoom(String roomNumber, String message, int toUserId) {
//        System.out.println("1번 세션 msg = " + message);
//        List<WebSocketSession> sessions = roomSessionsMap.get(String.valueOf(toUserId));
//
//        // 해당 유저가 지금 접속중이 아닐 때
//        if(sessions == null) {
//            return;
//        }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

        // 메시지 발송
        String msg = message.getPayload();
        JSONObject obj = jsonToObjectParser(msg);
        System.out.println(obj.toJSONString());

        String rN = (String) obj.get("roomNumber");
        HashMap<String, Object> temp = new HashMap<String, Object>();

        if (rls.size() > 0) {
            for (int i = 0; i < rls.size(); i++) {
                String roomNumber = (String) rls.get(i).get("roomNumber"); // 세션리스트의 저장된 방번호를 가져와서
                if (roomNumber.equals(rN)) { // 같은 값의 방이 존재한다면
                    temp = rls.get(i); // 해당 방 번호의 세션리스트의 존재하는 모든 object 값을 가져온다.
                    break;
                } else {
                }
            }

            // 해당 방의 세션들만 찾아서 메시지를 발송해준다.
            for (String k : temp.keySet()) {
                if (k.equals("roomNumber")) { // 다만 방 번호일 경우에는 건너뛴다.
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
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        SecurityContext securityContext = (SecurityContext) session.getAttributes().get("SPRING_SECURITY_CONTEXT");
        Authentication authentication = securityContext.getAuthentication();
        UserDto currentUser = null;

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            WebUserDetails userDetails = null;

            // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
            if (authentication instanceof OAuth2AuthenticationToken) {
                userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
            } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                userDetails = (WebUserDetails) authentication.getPrincipal();
            }
            currentUser = userDetails.getUser();
        }

//        Authentication authentication = securityContext.getAuthentication();
//        WebUserDetails userDetails = (WebUserDetails) authentication.getPrincipal();
//        UserDto currentUser = userDetails.getUser();


        // 1번방 소켓 종료
        String uri = session.getUri().toString();
        String roomNumber = extractRoomNumberFromUri(uri);
//        if (session != null && session.isOpen()) {
//            roomSessionsMap.computeIfPresent(String.valueOf(currentUser.getUserId()), (key, sessions) -> {
//                sessions.remove(session);
//                return sessions.isEmpty() ? null : sessions; // 해당 리스트가 비어있을 때만 제거함
//            });
//
//            session.close();
//            System.out.println(roomNumber + "번방 세션 닫힘" + ", 세션 ID: " + session.getId());
//        }

        // 채팅방 소켓 종료
        if (rls.size() > 0) { // 소켓이 종료되면 해당 세션값들을 찾아서 지운다.
            for (int i = 0; i < rls.size(); i++) {
                HashMap<String, Object> currentMap = rls.get(i);
                // 방 번호가 일치하는지 확인
                if (currentMap.get("roomNumber").equals(roomNumber)) {
                    currentMap.remove(session.getId()); // 해당 세션만 제거
                    if (currentMap.size() == 1) { // 아이디만 남은 경우에 해당 건 제거
                        rls.remove(i);
                    }
                    break;
                }
            }
        }
        String userId = String.valueOf(currentUser.getUserId());
        if (!users.isEmpty()) {
            users.removeIf(user -> user.equals(userId));
        }
        super.afterConnectionClosed(session, status);
    }

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
}