package com.boyug.controller;

import com.boyug.dto.BoyugProgramDto;
import com.boyug.dto.NotificationDto;
import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.WebUserDetails;
import com.boyug.security.jwt.JwtUtil;
import com.boyug.service.HomeService;
import com.boyug.service.NotificationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final NotificationService notificationService;
    private final HomeService homeService;
    private final JwtUtil jwtUtil;

    @RequestMapping(path = {"/", "/home"})
    public String home(Model model) {

        // 보육 프로그램 리스트 최근 20개
        Page<BoyugProgramDto> boyugProgramList = homeService.getLatestBoyugProgramList();
        model.addAttribute("boyugProgramList", boyugProgramList);
        return "home";
    }

    @RequestMapping(path = {"/module"})
    public String moudle() {
        return "module";
    }

    @GetMapping("/navbar")
    public String navbarShow(Model model, HttpServletRequest request,
                             @RequestParam(defaultValue = "0") Integer page,
                             @RequestParam(defaultValue = "5") int size) {

        // 현재 로그인 사용자 구분
        WebUserDetails userDetails = getUserDetails();

        // 이걸로 로직 테스트 함 해봐
        int userIdByToken = getUserIdByToken(request);

        if (userDetails != null) {
            List<NotificationDto> notifications = (userDetails != null)
                    ? notificationService.getUnreadNotifications(userDetails.getUser(), page, size)
                    : Collections.emptyList();
            model.addAttribute("notifications", notifications);
            model.addAttribute("loginUserId", userDetails.getUser().getUserId());
        } else {
            model.addAttribute("notifications", Collections.emptyList());
        }
        return "userView/modules/navbar";
    }

    @GetMapping("/notification/more")
    @ResponseBody
    public List<NotificationDto> notificationMore(@RequestParam(defaultValue = "0") Integer page,
                                                  @RequestParam(defaultValue = "5") int size) {
        // 알람창 paging
        WebUserDetails userDetails = getUserDetails();
        return  (userDetails != null)
                ? notificationService.getUnreadNotifications(userDetails.getUser(), page, size)
                : null;
    }

    // Admin Page 접근시 권한없으면 return
    @GetMapping("/login-denied")
    public String loginDeniedShow() {
        return "userView/login-denied";
    }

    // 토큰 추출
    private int getUserIdByToken(HttpServletRequest request) {
        // 쿠키에서 JWT 추출
        String token = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        int userId = 0;
        if (token == null) {
            return userId;
        }
        return jwtUtil.getUserId(token, "access");
    }

    // 로그인 유저
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
