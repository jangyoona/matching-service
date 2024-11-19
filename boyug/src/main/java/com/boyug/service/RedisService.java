package com.boyug.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    // 채팅방 userID 저장
    public void addUserToRoomNumber(String roomNumber, String userId) {
        redisTemplate.opsForSet().add("room:" + roomNumber, userId);
    }

    // 해당 RoomNumber가 존재하는지 조회
    public boolean isRoomExists(String roomNumber) {
        Boolean exists = redisTemplate.hasKey("room:" + roomNumber);
        return exists != null && exists;
    }

    // 해당 유저가 채팅방에 있는지 조회
    public boolean isUserInRoomNumber(String roomNumber, String userId) {
        return redisTemplate.opsForSet().isMember("room:" + roomNumber, userId);
    }

    // 웹소켓 세션 id 저장
    public void addSessionToRoomNumber(String roomNumber, String sessionId) {
        redisTemplate.opsForSet().add("room:" + roomNumber, sessionId);
    }

    // 웹소켓 세션 id 조회
    public boolean getSessionToRoomNumber(String roomNumber, String sessionId) {
        return redisTemplate.opsForSet().isMember("room:" + roomNumber, sessionId);
    }

    // 세션 아이디 제거
    public void removeSessionFromRoomNumber(String roomNumber, String sessionId) {
        redisTemplate.opsForSet().remove("room:" + roomNumber, sessionId);
    }

    // 채팅방 사용자 제거
    public void removeUserFromRoomNumber(String roomNumber, String userId) {
        redisTemplate.opsForSet().remove("room:" + roomNumber, userId);
    }



}
