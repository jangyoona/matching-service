package com.boyug.service;

import com.boyug.dto.ChatMessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Autowired

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void handleChatEvent(ChattingEvent event) {
        notificationService.insertNotification(
                event.getFromUser(),
                event.getToUser(),
                event.getChatRoom(),
                event.getNotificationMessage(),
                event.getMessage2()
        );
    }
}
