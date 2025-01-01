package com.boyug.websocket;

import com.boyug.entity.ChatMessageEntity;
import com.boyug.repository.AccountRepository;
import com.boyug.repository.ChatMessageRepository;
import com.boyug.repository.ChatRoomRepository;
import com.boyug.security.WebUserDetails;
import com.boyug.service.RedisService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SocketHandler extends TextWebSocketHandler {
    // https://myhappyman.tistory.com/101
    // https://micropilot.tistory.com/category/Spring%204/WebSocket%20with%20Interceptor

    private final TransactionTemplate transactionTemplate;

    // Redis Service
    private final RedisService redisService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;

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

        Principal user = session.getPrincipal();
        if (user == null) { // 인증 정보 없으면
            session.close(CloseStatus.NOT_ACCEPTABLE); // 연결 종료
            return;
        }

        // 메세지
        String msg = message.getPayload();

        // 주기적으로 서버-클라이언트 상태를 확인
        if (msg.equals(("ping"))) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        // 메시지 발송
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

                // 소켓 종료 시 Redis 에 저장된 채팅 DB 저장
                saveMessagesToDB(roomNumber);
            }
            super.afterConnectionClosed(session, status);
        }
    }


    private void saveMessagesToDB(String roomNumber) {
        transactionTemplate.execute(status -> {
            List<String> messages = redisService.findMessagesByChatRoomId("messages:" + roomNumber);

            if (messages != null && !messages.isEmpty()) {
                // 메세지 Entity 로 변환
                List<ChatMessageEntity> entity = convertRedisMessagesToEntities(messages);

                // DB 일괄 저장
                chatMessageRepository.saveAll(entity);

                // 저장 후 Redis 데이터 삭제
                redisService.deleteMessagesByChatRoomId("messages:" + roomNumber);
            }
            return null;
        });
    }

    @NotNull
    private List<ChatMessageEntity> convertRedisMessagesToEntities(List<String> messages) {
        return messages.stream().map(msg -> {
            String[] parts = msg.split("\\|");

            return new ChatMessageEntity(
                    chatRoomRepository.findById(Integer.parseInt(parts[0])), // chatRoomId
                    parts[1],                                                // chatContent
                    Timestamp.valueOf(parts[2]),                             // chatSendTime
                    accountRepository.findById(Integer.parseInt(parts[3])),  // fromUser
                    accountRepository.findById(Integer.parseInt(parts[4])),  // toUser
                    false                                                    // toIsRead
            );
        }).collect(Collectors.toList());
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

    // JWT 토큰용
    private WebUserDetails getUserInfo(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        WebUserDetails userDetails = null;

        if (principal != null && principal instanceof UsernamePasswordAuthenticationToken authentication) {
            // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
            if (authentication.isAuthenticated()) {
                userDetails = (WebUserDetails) authentication.getPrincipal();
            }
        }

        return userDetails;
    }

    // JWT 방식으로 바꾸면 SPRING_SECURITY_CONTEXT (세션)에 속성이 채워지지 않음. 아래는 세션 방식일 때 쓰는 놈임.
//    private WebUserDetails getUserInfo(WebSocketSession session) {
//        SecurityContext securityContext = (SecurityContext) session.getAttributes().get("SPRING_SECURITY_CONTEXT");
//        Authentication authentication = securityContext.getAuthentication();
//        WebUserDetails userDetails = null;
//
//        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
//
//            // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
//            if (authentication instanceof OAuth2AuthenticationToken) {
//                userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
//            } else if (authentication.getPrincipal() instanceof WebUserDetails) {
//                userDetails = (WebUserDetails) authentication.getPrincipal();
//            }
//        }
//
//        return userDetails;
//    }
}