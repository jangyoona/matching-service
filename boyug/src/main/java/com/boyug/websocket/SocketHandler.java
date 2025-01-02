package com.boyug.websocket;

import com.boyug.dto.ChatMessageVO;
import com.boyug.entity.ChatMessageEntity;
import com.boyug.repository.AccountRepository;
import com.boyug.repository.ChatMessageRepository;
import com.boyug.repository.ChatRoomRepository;
import com.boyug.security.WebUserDetails;
import com.boyug.service.RedisService;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SocketHandler extends TextWebSocketHandler {

    private final TransactionTemplate transactionTemplate;

    // Redis Service
    private final RedisService redisService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;

    // Redis pub/sub
    private final RedisSubscriberConfig redisSubscriberConfig;
    private final RedisSubscriber redisSubscriber;

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //소켓 연결
        super.afterConnectionEstablished(session);

        String url = session.getUri().toString();
        String roomNumber = url.split("/chatting/")[1];

        // 현재 사용자 정보
        WebUserDetails loginUser = (WebUserDetails) session.getAttributes().get("loginUser");
        String userId = String.valueOf(loginUser.getUser().getUserId());

        // key 생성
        String sessionKey = roomNumber + ":" + userId;

        // 기존 세션 종료 (중복 방지)
        if (sessions.containsKey(sessionKey)) {
            WebSocketSession oldSession = sessions.remove(sessionKey);
            try {
                oldSession.close(); // 기존 세션 강제 종료
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 새 세션 등록
        sessions.put(sessionKey, session);

        // Redis 채팅방 접속자 (알림 유무 구분 용도)
        redisService.addUserToRoomNumber(roomNumber, userId);

        // Redis 구독
        redisSubscriberConfig.subscribe(sessionKey, redisSubscriber);
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
        }
//        // UI용 메세지 발송
//        JSONObject obj = jsonToObjectParser(msg);
//        WebSocketSession ws = sessions.get(obj.get("fromUserId"));
//        if (ws != null && ws.isOpen()) {
//            ws.sendMessage(new TextMessage(obj.toJSONString()));
//        }
    }

    public void sendToUser(ChatMessageVO message) throws IOException {

        JSONObject obj = new JSONObject();
        obj.put("roomNumber", message.getRoomNumber());
        obj.put("message", message.getMessage());
        obj.put("fromUserId", message.getFromUserId());
        obj.put("toUserId", message.getToUserId());

        // From
        String fromSessionKey = message.getRoomNumber() + ":" + message.getFromUserId();
        sendMessageToSynchronized(fromSessionKey, obj);

        // To
        String toSessionKey = message.getRoomNumber() + ":" + message.getToUserId();
        sendMessageToSynchronized(toSessionKey, obj);

    }
    // 동기화 블록 -> 세션 있으면 메세지 전송
    private void sendMessageToSynchronized(String sessionKey, JSONObject obj) throws IOException {
        WebSocketSession session = sessions.get(sessionKey);
        if (session != null) {
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(obj.toJSONString()));
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

            // key 생성
            String sessionKey = roomNumber + ":" + userId;

            // 세션 제거
            sessions.remove(sessionKey);

            boolean userInRoomNumber = redisService.isUserInRoomNumber(roomNumber, userId);

            if (userInRoomNumber) {
                if (!userId.equals("")) {
                    redisService.removeUserFromRoomNumber(roomNumber, userId);
                }
                // 소켓 종료 시 Redis 에 저장된 채팅 DB 저장
                saveMessagesToDB(roomNumber);
            }

            // Redis 구독 해제
            redisSubscriberConfig.unsubscribe(sessionKey);

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