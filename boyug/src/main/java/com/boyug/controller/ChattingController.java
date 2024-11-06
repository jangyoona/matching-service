package com.boyug.controller;

import com.boyug.dto.*;
import com.boyug.entity.ChatMessageEntity;
import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.WebUserDetails;
import com.boyug.service.AccountService;
import com.boyug.service.ChattingService;
import com.boyug.service.NotificationService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ModelAndView chatting(@RequestParam HashMap<Object, Object> params, HttpSession session) {
        // 채팅방
        ModelAndView mv = new ModelAndView();
        int roomNumber = Integer.parseInt((String) params.get("roomNumber"));
        ChatRoomDto isChatRoom = chattingService.getChatRoom(roomNumber);

        // 현재 로그인 사용자 구분
        WebUserDetails userDetails = getUserDetails();
        UserDto loginUser = userDetails.getUser();
        mv.addObject("loginUser", loginUser);


        // 1 otherUser 추출
        UserDto otherUser = null;
        if(loginUser.getUserId() == isChatRoom.getChatMessages().get(0).getToUserId().getUserId()) {
            otherUser = accountService.getUserInfo(isChatRoom.getChatMessages().get(0).getFromUserId().getUserId());
        } else {
            otherUser = accountService.getUserInfo(isChatRoom.getChatMessages().get(0).getToUserId().getUserId());
        }

        // 2 채팅방 헤더 name
        String otherUserName = "";

        if (otherUser.getUserCategory() == 2) {
            BoyugUserDto boyugUser = accountService.getBoyugUserInfo(otherUser.getUserId());
            otherUserName = boyugUser.getBoyugUserName();
        } else if (otherUser.getUserCategory() == 3) {
            otherUserName = otherUser.getUserName();
        }
        mv.addObject("otherUserName", otherUserName);


        if(isChatRoom != null) {
            mv.addObject("roomNumber", params.get("roomNumber"));
            mv.setViewName("chat");

            // 채팅방 목록 상대방 ProfileImage List
            List<ChatRoomDto> userRoomList = chattingService.getChatRoomByUserId(loginUser);
            int userCategory = loginUser.getUserCategory();

            List<ProfileImageDto> profileImage = accountService.getUserProfileImage(otherUser);
            String profileUrl = (profileImage != null && !profileImage.isEmpty()) ? profileImage.get(0).getImgSavedName() : null; // 채팅방 상대 프로필

            List<ProfileImageDto> profileImages = null;
            for (ChatRoomDto chatRoomDto : userRoomList) {
                if((userCategory == 2 && chatRoomDto.isBoyugChatActive()) || (userCategory == 3 && chatRoomDto.isUserChatActive())) {
                    if (chatRoomDto.getChatMessages().get(0).getToUserId().getUserId() != loginUser.getUserId()) {
                        profileImages = accountService.getUserProfileImage(chatRoomDto.getChatMessages().get(0).getToUserId());
                        chatRoomDto.getChatMessages().get(0).getToUserId().setImages(profileImages);
                    } else {
                        profileImages = accountService.getUserProfileImage(chatRoomDto.getChatMessages().get(0).getFromUserId());
                        chatRoomDto.getChatMessages().get(0).getFromUserId().setImages(profileImages);
                    }
                }
            }
            mv.addObject("userRoomList", userRoomList); // 채팅창 왼쪽 목록에 띄울 리스트
            mv.addObject("profileUrl", profileUrl); // 채팅창 상대방 프로필 url


            // 기존 채팅 불러오기
            List<ChatMessageDto> messages = chattingService.getMessagesByRoomId(roomNumber);
            for (ChatMessageDto message : messages) {
                if (message.getToUserId().getUserId() != loginUser.getUserId()) {
                    message.getToUserId().setImages(profileImage);
                } else if (message.getFromUserId().getUserId() != loginUser.getUserId()) {
                    message.getFromUserId().setImages(profileImage);
                }
            }
            mv.addObject("messages", messages);

            // 메세지 send시 option에 담길 toUserId를 위해 저장
            int toUserId = 0;

            // from, to 유저 서버에 저장
            roomUser.put(roomNumber+"f", loginUser.getUserId());
            if(messages.isEmpty()){ // 최초 생성시 이건 잘 작동함
                roomUser.put(roomNumber+"t", session.getAttribute("toUserId"));
            } else {
                if(messages.get(0).getToUserId().getUserId() == userDetails.getUser().getUserId()) {
                    roomUser.put(roomNumber+"t", messages.get(0).getFromUserId().getUserId());
                    mv.addObject("toUserName", messages.get(0).getFromUserId().getUserName());
                    toUserId = messages.get(0).getFromUserId().getUserId();
                } else {
                    roomUser.put(roomNumber+"t", messages.get(0).getToUserId().getUserId());
                    mv.addObject("toUserName", messages.get(0).getToUserId().getUserName());
                    toUserId = messages.get(0).getToUserId().getUserId();
                }
            }
            mv.addObject("toUserId", toUserId);
        } else {
            mv.setViewName("home?chaterror");
        }
        return mv;
    }



    @GetMapping("sendMessage")
    @ResponseBody
    public String sendMessage(String message, String roomNumber, String uri, Model model) {

        // 현재 로그인 사용자
        WebUserDetails userDetails = getUserDetails();

        // 받는사람 추출
        int toUserId = getToUserId(roomNumber, userDetails);
        model.addAttribute("toUserId", toUserId);

        chattingService.sendMessage(userDetails, roomNumber, message, toUserId);
        return "success";
    }

    private int getToUserId(String roomNumber, WebUserDetails userDetails) {
        int toUserId = 0;

        String fromUserIdStr = roomUser.get(roomNumber + "f").toString();
        String targetUserIdStr = roomUser.get(roomNumber + "t").toString();

        if (userDetails.getUser().getUserId() == Integer.parseInt(fromUserIdStr)) {
            toUserId = Integer.parseInt(targetUserIdStr);
        } else {
            toUserId = Integer.parseInt(fromUserIdStr);
        }
        return toUserId;
    }



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
