package com.boyug.controller;

import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.WebUserDetails;
import com.boyug.service.ActivityService;
import com.boyug.service.ChattingService;
import com.boyug.service.NotificationService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
public class NotificationController {

    @Setter(onMethod_ = { @Autowired})
    NotificationService notificationService;

    @Setter(onMethod_ = { @Autowired})
    ChattingService chattingService;

    @Setter(onMethod_ = { @Autowired})
    ActivityService activityService;


    @GetMapping("/notifications/{userId}")
    public SseEmitter streamNotifications(@PathVariable String userId) {
        return notificationService.createConnection(userId);
    }

    @GetMapping("/notification/chat-read")
    public String markChatNotificationsAsRead(int notificationId, int chatRoomId) {
        // 알림, 메세지 읽음 처리
        WebUserDetails userDetails = getUserDetails();
        notificationService.markAsRead(notificationId);
        chattingService.updateChatMessageIsRead(chatRoomId, userDetails.getUser().getUserId());

        return "success";
    }

    @GetMapping("/notification/read")
    public String markNotificationsAsRead(int notificationId) {
        // 일반알림 읽음 처리 (알림 구성 추가시 사용)
        notificationService.markAsRead(notificationId);

        return "success";
    }

    @GetMapping("/notification/delete")
    public String notificationDelete(int notificationId) {
        notificationService.markAsRead(notificationId);
        return "success";
    }

    @GetMapping("notification/allDelete")
    public String notificationAllDelete(int userId) {
        notificationService.markAsReadAll(userId);
        return "success";
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


//    기존 웹소켓용 알림
//@Controller
//public class NotificationController {

//    @GetMapping("/notification")
//    @ResponseBody
//    public List<NotificationDto> notificationShow() {
//        //
//        // 문제 1. 메인에서만 메서드 불러짐. 알림 클릭시 불러오는걸로 바꿔야 될듯? 그럼 알림은 어떡하지.
//        //
//        // 현재 로그인 사용자
//        WebUserDetails userDetails = getUserDetails();
//
//        // 알림 목록 가져오기
//        List<NotificationDto> notifications = notificationService.getUnreadNotifications(userDetails.getUser());
//
//        return notifications;
//    }
//

//

//

//
//

}
