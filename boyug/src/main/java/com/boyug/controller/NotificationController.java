package com.boyug.controller;

import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.WebUserDetails;
import com.boyug.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping("/notifications/{userId}")
    public SseEmitter streamNotifications(@PathVariable String userId) {
        return notificationService.createConnection(userId);
    }

    @GetMapping("/notification/chat-read")
    public ResponseEntity<Boolean> markChatNotificationsAsRead(int notificationId, int chatRoomId) {

        WebUserDetails userDetails = getUserDetails();

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        try {
            // 알림, 채팅(메세지) 읽음 처리
            notificationService.processNotifications(notificationId, chatRoomId, userDetails.getUser().getUserId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping("/notification/read")
    public ResponseEntity<Boolean> markNotificationsAsRead(int notificationId) {
        // 일반알림 읽음 처리 (알림 구성 추가시 사용)
        try {
            notificationService.markAsRead(notificationId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
        return ResponseEntity.ok(true);

    }

    @GetMapping("/notification/delete")
    public ResponseEntity<Boolean> notificationDelete(int notificationId) {
        try {
            notificationService.markAsRead(notificationId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping("notification/allDelete")
    public ResponseEntity<Boolean> notificationAllDelete(int userId) {
        try {
            notificationService.markAsReadAll(userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping("notification/{sessionid}")
    @ResponseBody
    public String boyugToUserSendNotification() {

        return "success";
    }




    private WebUserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebUserDetails userDetails = null;

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

            // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
            if (authentication instanceof OAuth2AuthenticationToken) {
                userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
            } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                userDetails = (WebUserDetails) authentication.getPrincipal();
            }
        }
        return userDetails;
    }

}
