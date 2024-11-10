package com.boyug.controller;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentLogger {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentLogger.class);

    @PostConstruct
    public void logEnvironmentVariables() {
        logger.info("DB URL: {}", System.getenv("DB_URL"));
        logger.info("DB Username: {}", System.getenv("DB_USERNAME"));
    }
}
