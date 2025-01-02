package com.boyug.websocket;

import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RedisSubscriberConfig {

    private final RedisMessageListenerContainer container;
    private final Map<String, MessageListenerAdapter> listenerMap = new ConcurrentHashMap<>();

    public RedisSubscriberConfig(RedisMessageListenerContainer container) {
        this.container = container;
    }


    // 구독 등록
    public void subscribe(String key, MessageListener listener) {

        // 기존 구독 해제 (중복 방지)
        unsubscribe(key);

        // 리스너 캐싱
        MessageListenerAdapter adapter = new MessageListenerAdapter(listener);
        listenerMap.put(key, adapter);

        // 구독 등록
//        String channel = roomNumber + ":" + userId;
        container.addMessageListener(adapter, new ChannelTopic(key));

        if (!container.isRunning()) {
            container.start(); // start()가 동기적이므로 재시작 방지
        }
        System.out.println("구독완료 --------> " + key);
    }
    
    // 구독 해제
    public void unsubscribe(String key) {

        // 캐시된 리스너가 있는지 확인
        if (listenerMap.containsKey(key)) {
            container.removeMessageListener(listenerMap.get(key), new ChannelTopic(key)); // 리스너 해제
            listenerMap.remove(key);
            System.out.println("구독 해제 --------> " + key);
        } else {
            System.out.println("구독 해제 실패(리스너 없음): " + key);
        }
    }

}
