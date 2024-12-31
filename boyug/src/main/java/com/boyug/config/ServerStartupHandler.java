package com.boyug.config;


import com.boyug.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServerStartupHandler {

    private RedisService redisService;

    public ServerStartupHandler(RedisService redisService) {
        this.redisService = redisService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        redisService.initRedis();
        log.info("-------------------Application Server Open-------------------");
    }
}
