package com.boyug.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class RememberMeTokenCleanScheduler {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(14);  // 14일 지난 토큰
        String sql = "DELETE FROM persistent_logins WHERE last_used < ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(expiryDate));  // 삭제

        System.out.println("Expired tokens cleaned up.");
    }
}
