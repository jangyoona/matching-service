package com.boyug.controller;

import com.boyug.common.SmsApi;
import com.boyug.dto.*;
import com.boyug.entity.ProfileImageEntity;
import com.boyug.entity.SessionEntity;
import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.repository.AccountRepository;
import com.boyug.security.WebUserDetails;
import com.boyug.service.ActivityService;
import com.boyug.service.BookmarkService;
import lombok.Setter;
import lombok.extern.flogger.Flogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(path = { "/userView/activityPages" })
public class ActivityPagesController {

    @Setter(onMethod_ = {@Autowired})
    private ActivityService activityService;

    @Setter(onMethod_ = {@Autowired})
    private BookmarkService bookmarkService;

    @Setter(onMethod_ = { @Autowired })
    private SmsApi smsApi;

    @GetMapping(path = { "/boyugProgramList" })
    public String boyugProgramListForm(Model model,
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "12") int size) {

        Page<BoyugProgramDto> boyugProgramList = activityService.findAllList(page, size);


        model.addAttribute("boyugProgramList", boyugProgramList);

        // 페이지 정보를 모델에 추가 (선택사항)
        model.addAttribute("currentPage", boyugProgramList.getNumber());
        model.addAttribute("totalPages", boyugProgramList.getTotalPages());
        model.addAttribute("totalElements", boyugProgramList.getTotalElements());


        return "/userView/activityPages/boyugProgramList";
    }


    // start 보육원 글 쓰기 화면/ 작성 컨트롤러

    @GetMapping(path = { "/boyugProgram" })
    public String boyugProgramForm(Model model) {

        List<SessionDto> sessions = activityService.findAllSessions();
        model.addAttribute("sessions", sessions);

        return "/userView/activityPages/boyugProgram";
    }

    @PostMapping(path = { "/boyugProgramWrite" })
    public String boyugProgram(@ModelAttribute BoyugProgramDto program) {

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        // String username = authentication.getName();
//        WebUserDetails userDetails = (WebUserDetails)authentication.getPrincipal();
//        UserDto user = userDetails.getUser();

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

        int userId = userDetails.getUser().getUserId();

        program.setUserId(userId);

        //int userId = activityService.findUserId(userId);

        try {
            activityService.writeBoyugProgram(program);
            return "redirect:/userView/activityPages/boyugProgramList";
        } catch (Exception ex) {
            System.out.println("fail");
            System.out.println(ex);
            return "redirect:/userView/activityPages/boyugProgram";
        }
    }

    // end 보육원 글 쓰기 화면/ 작성 컨트롤러

    @GetMapping(path = { "/boyugProgramDetail" })
    public String boyugProgramDetailForm(@RequestParam(value = "boyugProgramId") Integer boyugProgramId,
                                         Model model) {
        BoyugProgramDto boyugProgram = activityService.findBoyugProgram(boyugProgramId);

        boolean isAuthor = false;

        // 로그인한 유저의 정보 갖고오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            WebUserDetails userDetails = null;

            // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
            if (authentication instanceof OAuth2AuthenticationToken) {
                userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
            } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                userDetails = (WebUserDetails) authentication.getPrincipal();
            }
            int userId = userDetails.getUser().getUserId();
            // 갖고 옴

            isAuthor = boyugProgram.getUserId() == userId;

            for (BoyugProgramDetailDto programDetail : boyugProgram.getProgramDetails()) {
                int detailId = programDetail.getBoyugProgramDetailId();
                boolean isApplied = activityService.isUserApplied(userId, detailId);

                programDetail.setApplied(isApplied);
            }
            // Bookmark
            boolean isBookmark = bookmarkService.isProgramBoardBookmarked(userId, boyugProgram.getUserId());
            model.addAttribute("bookmark", isBookmark);
        }

        model.addAttribute("boyugProgram", boyugProgram);
        model.addAttribute("isAuthor", isAuthor);

        return "/userView/activityPages/boyugProgramDetail";
    }

    @GetMapping(path = { "/boyugProgramEdit" })
    public String boyugProgramEditForm(@RequestParam(value = "boyugProgramId") Integer boyugProgramId,
                                         Model model) {

        BoyugProgramDto boyugProgram = activityService.findBoyugProgram(boyugProgramId);
        List<SessionDto> sessions = activityService.findAllSessions();

        boolean isAuthor = false;

        // 로그인한 유저의 정보 갖고오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            WebUserDetails userDetails = null;

            // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
            if (authentication instanceof OAuth2AuthenticationToken) {
                userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
            } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                userDetails = (WebUserDetails) authentication.getPrincipal();
            }
            int userId = userDetails.getUser().getUserId();
            // 갖고 옴

            isAuthor = boyugProgram.getUserId() == userId;

        }

        model.addAttribute("boyugProgram", boyugProgram);
        model.addAttribute("isAuthor", isAuthor);
        model.addAttribute("sessions", sessions);

        if (isAuthor) {
            return "/userView/activityPages/boyugProgramEdit";
        } else {
            return "redirect:/userView/activityPages/boyugProgramList";
        }

    }

    @PostMapping(path = { "/boyugProgramEdit" })
    public String boyugProgramEdit(@ModelAttribute BoyugProgramDto program) {

        try {
            activityService.editBoyugProgram(program);
            return "redirect:/userView/activityPages/boyugProgramDetail?boyugProgramId=" + program.getBoyugProgramId();
        } catch (Exception ex) {
            System.out.println("fail");
            System.out.println(ex);
            return "redirect:/userView/activityPages/boyugProgramEdit?boyugProgramId=" + program.getBoyugProgramId();
        }
    }

    @ResponseBody
    @GetMapping(path = { "/apply-userToBoyug" })
    public String apllyUserToBoyug(@RequestParam(value = "detailNo") int detailNo ) {
        try {


            WebUserDetails userDetails = null;
            // 로그인한 유저의 정보 갖고오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

                // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
                if (authentication instanceof OAuth2AuthenticationToken) {
                    userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
                } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                    userDetails = (WebUserDetails) authentication.getPrincipal();
                }
            }
            int userId = userDetails.getUser().getUserId();
            // 갖고 옴
            activityService.insertUserToBoyug(userId, detailNo);
//        System.out.println("userId : " + userId + ", detailNo : " + detailNo);
            return "success";
        } catch (Exception ex) {
            System.out.println(ex);
            return "error";
        }
    }

    @GetMapping(path = { "/noinRegister" })
    public String noinRegisterForm(Model model) {

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

        int userId = userDetails.getUser().getUserId();

        // 선호 목록 갖고오기
        List<SessionDto> favorites = activityService.findUserFavorites(userId);

        // 현재 등록되어있는지 아닌지 확인
        boolean active = activityService.findRegisterUser(userId);

        // 이미지 갖고오기
        ProfileImageDto profile = activityService.findProfileImage(userId);

//        int userId = userDetails.getUser().getUserId();
        model.addAttribute("userDetails", userDetails);
        model.addAttribute("favorites", favorites);
        model.addAttribute("active", active);
        model.addAttribute("profile", profile);

        return "/userView/activityPages/noinRegister";
    }

    @ResponseBody
    @GetMapping(path = { "/noinRegister2" })
    public String noinRegister() {

        try {
            // 로그인한 유저의 정보 갖고오기
            WebUserDetails userDetails = null;
            // 로그인한 유저의 정보 갖고오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

                // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
                if (authentication instanceof OAuth2AuthenticationToken) {
                    userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
                } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                    userDetails = (WebUserDetails) authentication.getPrincipal();
                }
            }
            int userId = userDetails.getUser().getUserId();

            activityService.insertNoinRegister(userId);

            return "success";
        } catch (Exception ex) {
            return "fail";
        }
    }

    @ResponseBody
    @GetMapping(path = { "/noinUnRegister" })
    public String noinUnRegister() {

        try {
            // 로그인한 유저의 정보 갖고오기
            WebUserDetails userDetails = null;
            // 로그인한 유저의 정보 갖고오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

                // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
                if (authentication instanceof OAuth2AuthenticationToken) {
                    userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
                } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                    userDetails = (WebUserDetails) authentication.getPrincipal();
                }
            }
            int userId = userDetails.getUser().getUserId();

            activityService.updateNoinUnRegister(userId);

            return "success";
        } catch (Exception ex) {
            return "fail";
        }
    }

    @GetMapping(path = { "/delete-boyugProgram" })
    public String deleteBoyugProgram(@RequestParam(value ="boyugProgramId") int boyugProgramId) {
        try {
            // 로그인한 유저의 정보 갖고오기
            WebUserDetails userDetails = null;
            // 로그인한 유저의 정보 갖고오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

                // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
                if (authentication instanceof OAuth2AuthenticationToken) {
                    userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
                } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                    userDetails = (WebUserDetails) authentication.getPrincipal();
                }
            }
            int userId = userDetails.getUser().getUserId();

            Boolean success = activityService.deleteBoyugProgram(boyugProgramId, userId);

            if (success) {
                return "redirect:/userView/activityPages/boyugProgramList";
            } else {
                return "redirect:/userView/activityPages/boyugProgramDetail?boyugProgramId=" + boyugProgramId;
            }
        } catch (Exception ex) {
            System.out.println(ex);
            return "redirect:/userView/activityPages/boyugProgramDetail?boyugProgramId=" + boyugProgramId;
        }
    }

    @GetMapping(path = "/noinRegisterList")
    public String noinRegisterListForm(Model model,
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "12") int size) {


        Page<UserDto> users = activityService.findNoinRegisterList(page, size);

        model.addAttribute("users", users);

        model.addAttribute("currentPage", users.getNumber());
        model.addAttribute("totalPages", users.getTotalPages());

        return "/userView/activityPages/noinRegisterList";
    }

    @GetMapping(path = { "/noinRegisterDetail" })
    public String noinRegisterDetailForm(@RequestParam(value = "userId") int userId, Model model) {

        // 선택한 유저의 정보를 가져오기
        UserDto user = activityService.findNoinRegisterUser(userId);
        model.addAttribute("user", user);

        // 내가 구인중인 활동목록 가져오기

        // 로그인한 유저의 정보 갖고오기
        WebUserDetails userDetails = null;
        // 로그인한 유저의 정보 갖고오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

            // OAuth2AuthenticationToken인 경우 변환, 아니면 WebUserDetails를 직접 할당
            if (authentication instanceof OAuth2AuthenticationToken) {
                userDetails = WebUserDetails.of((CustomOAuth2User) authentication.getPrincipal());
            } else if (authentication.getPrincipal() instanceof WebUserDetails) {
                userDetails = (WebUserDetails) authentication.getPrincipal();
            }
        }
        int loginUserId = userDetails.getUser().getUserId();

        List<BoyugProgramDetailDto> programDetails = activityService.findBoyugProgramDetails(loginUserId, userId);
        model.addAttribute("programDetails", programDetails);

        return "/userView/activityPages/noinRegisterDetail";
    }

    @PostMapping(path = { "/boyugToUser" })
    public String boyugToUser(@RequestParam(value ="checkedValue") List<Integer> checkedValue, @RequestParam(value ="userId") int userId) {

        BoyugToUserDto boyugToUserDto = new BoyugToUserDto();
        boyugToUserDto.setUserSessionId(userId);
        activityService.insertBoyugToUser(checkedValue, userId);

        // sessionId로 유저 조회 및 알림 문자 전송
        UserDto toUser = activityService.findUserByUserSessionId(userId);
        if (toUser != null) {
            String message = "[Together] 활동 요청알림입니다. '내 정보 - 신청 내역'을 확인해 주세요.";
            smsApi.notificationSend(toUser.getUserPhone(), message);
        }
        return "redirect:/userView/activityPages/noinRegisterDetail?userId=" + userId;

    }

}
