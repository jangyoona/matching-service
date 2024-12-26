package com.boyug.controller;

import com.boyug.dto.*;
import com.boyug.entity.ChatMessageEntity;
import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.WebUserDetails;
import com.boyug.service.AccountService;
import com.boyug.service.ChattingService;
import com.boyug.service.NotificationService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChattingController {

    @Setter(onMethod_ = { @Autowired })
    ChattingService chattingService;

    @Setter(onMethod_ = { @Autowired })
    NotificationService notificationService;

    @Setter(onMethod_ = { @Autowired })
    AccountService accountService;

    // 생성된 방 번호에 들어온 유저 = roomNumber+f  OR  roomNumber+t
    static Map<String, Object> roomUser = new HashMap<>();

    public static Map<String, Object> getRoomUser() {
        return roomUser;
    }

    @GetMapping("/checkLoginStatus")
    public ResponseEntity<Boolean> checkLoginStatus() {
        try {
            WebUserDetails userDetails = getUserDetails();

            // null 만 반환할 가능성이 있어 null 체크 한번 하고
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        };
        return ResponseEntity.ok(true);
    };

    @RequestMapping("/chat")
    public ModelAndView chattingShow() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("chat");
        return mv;
    }

    @RequestMapping("createRoom")
    @ResponseBody
    public List<ChatRoomDto> createRoom(@RequestParam HashMap<Object, Object> params, HttpSession session) {
//                                        @RequestParam(value = "chatRoomId", required = false)String chatRoomId) {
        WebUserDetails userDetails = getUserDetails();
        int toUserId = Integer.parseInt((String) params.get("userId"));

        List<ChatRoomDto> roomList = chattingService.createOrGetChatRoom(userDetails, toUserId);

        session.setAttribute("toUserId", toUserId);
        return roomList;
    }

    @RequestMapping("moveChatting")
    public ModelAndView chatting(@RequestParam HashMap<Object, Object> params, HttpServletRequest request) {

        int roomNumber = Integer.parseInt((String) params.get("roomNumber"));
        WebUserDetails userDetails = getUserDetails();
        return chattingService.prepareChatView(roomNumber, userDetails, request);
    }



    @GetMapping("sendMessage")
    @ResponseBody
    public ResponseEntity<Boolean> sendMessage(String message, String roomNumber, Model model, Integer toUserId) {

        // 현재 로그인 사용자
        WebUserDetails userDetails = getUserDetails();

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        // 받는사람 추출
//        int toUserId = getToUserId(roomNumber, userDetails);
        model.addAttribute("toUserId", toUserId);

        chattingService.sendMessage(userDetails, roomNumber, message, toUserId);
        return ResponseEntity.ok(true);
    }

//    private int getToUserId(String roomNumber, WebUserDetails userDetails) {
//        int toUserId = 0;
//
//        String fromUserIdStr = roomUser.get(roomNumber + "f").toString();
//        String targetUserIdStr = roomUser.get(roomNumber + "t").toString();
//
//        if (userDetails.getUser().getUserId() == Integer.parseInt(fromUserIdStr)) {
//            toUserId = Integer.parseInt(targetUserIdStr);
//        } else {
//            toUserId = Integer.parseInt(fromUserIdStr);
//        }
//        return toUserId;
//    }



    @GetMapping("message-read")
    @ResponseBody
    public String chatMessageIsRead(int chatRoomId, int toUserId) {
        chattingService.updateChatMessageIsRead(chatRoomId, toUserId);
        return "success";
    }

    // 채팅방에서 목록 방 나가기
    @PostMapping("out-chatroom")
    @ResponseBody
     public String outChatRoom(int[] selectedValues) {
        WebUserDetails user = getUserDetails();
        chattingService.editChatRoomActive(user,selectedValues);

        return "success";
    }



    // 로그인한 유저 정보 function
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
