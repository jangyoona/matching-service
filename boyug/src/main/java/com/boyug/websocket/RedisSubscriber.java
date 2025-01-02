package com.boyug.websocket;

import com.boyug.dto.ChatMessageVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RedisSubscriber implements MessageListener {

    private final SocketHandler socketHandler;

    public RedisSubscriber(@Lazy SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern);
        String msg = message.toString();
        ChatMessageVO vo = parseMessage(msg);

        // 메시지 처리 및 전달
        try {
            socketHandler.sendToUser(vo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private ChatMessageVO parseMessage(String msg) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // 날짜 관련 처리 모듈 추가
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return objectMapper.readValue(msg, ChatMessageVO.class);
        } catch (Exception e) {
            throw new RuntimeException("Message parsing failed", e);
        }
    }
}
