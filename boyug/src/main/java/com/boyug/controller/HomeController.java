package com.boyug.controller;

import com.boyug.dto.*;
import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.WebUserDetails;
import com.boyug.service.AccountService;
import com.boyug.service.ActivityService;
import com.boyug.service.NotificationService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

    @Setter(onMethod_ = {@Autowired})
    NotificationService notificationService;

    @Setter(onMethod_ = {@Autowired})
    AccountService accountService;

    @Setter(onMethod_ = {@Autowired})
    ActivityService activityService;

    @RequestMapping(path = {"/", "/home"})
    public String home(Model model) {

        // 보육 프로그램 리스트 최근 20개
        Page<BoyugProgramDto> boyugProgramList = activityService.findAllList(0, 20);
        for (BoyugProgramDto program : boyugProgramList) {
            UserDto user = accountService.getUserInfo(program.getUserId());
            // 화면 출력용 주소 자르기
            String[] addrCut = user.getUserAddr2().split("\\s+");
            String addrSplit = String.format("%s %s", addrCut[0], addrCut[1]);
            user.setUserAddr2(addrSplit);

            List<ProfileImageDto> profile = accountService.getUserProfileImage(user);
            user.setImages(profile);
            program.setUser(user);

            if (program.getUser().getImages().isEmpty()) {
                // 프로필 이미지가 만약 없을 경우 기본 이미지 세팅
                ProfileImageDto basicImage = new ProfileImageDto();
                basicImage.setImgSavedName("no_img.jpg");
                profile.add(basicImage);
                user.setImages(profile);
            }
        }
        model.addAttribute("boyugProgramList", boyugProgramList);
        return "home";
    }

//    @RequestMapping(path = {"/noinHome"})
//    public String noinHome() {
//        return "noinHome";
//    }

    @RequestMapping(path = {"/module"})
    public String moudle() {
        return "module";
    }

    @GetMapping("/navbar")
    public String navbarShow(Model model, @RequestParam(defaultValue = "0") Integer page,
                                          @RequestParam(defaultValue = "5") int size) {

        // 현재 로그인 사용자 구분
        WebUserDetails userDetails = getUserDetails();
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
    @GetMapping("/admin/adminView/login-denied")
    public String loginDeniedShow() {
        return "adminView/login-denied";
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
