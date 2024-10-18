package com.boyug.controller;

import com.boyug.common.KaKaoApi;
import com.boyug.common.Matching;
import com.boyug.common.SmsApi;
import com.boyug.common.Util;
import com.boyug.dto.*;
import com.boyug.entity.BoyugProgramDetailEntity;
import com.boyug.security.WebUserDetails;
import com.boyug.service.BoyugMyPageService;
import com.boyug.service.PersonalService;
import com.boyug.ui.ProjectPager;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = {"/myPage"})
@RequiredArgsConstructor
public class BoyugMyPageController {

    @Setter(onMethod_ = {@Autowired})
    private BoyugMyPageService boyugMyPageService;

    @Setter(onMethod_ = {@Autowired})
    private PersonalService personalService;

    @Setter(onMethod_ = { @Autowired })
    private SmsApi smsApi;

    @Setter(onMethod_ = @Autowired)
    private JavaMailSender mailSender;

    private final KaKaoApi kaKaoApi;

    @GetMapping(path = {"/boyug-request"})
    public String boyugRequest(Model model, HttpServletRequest req,
                               @RequestParam(defaultValue = "1", name = "pageNo") int pageNo) {

        return "userView/boyugMyPage/boyug-request";
    }

    @GetMapping(path = {"/request-module"})
    public String requestModule(Model model) {
        // 내가 개인한테 신청 보낸 내역
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebUserDetails userDetails = (WebUserDetails)  authentication.getPrincipal();

        List<BoyugToUserDto> boyugToUsers = boyugMyPageService.findBoyugToUsers(userDetails.getUser().getUserId());
        List<UserDto> users = boyugMyPageService.findPersonalUsersWithBtu(boyugToUsers);
        List<BoyugProgramDetailDto> details = boyugMyPageService.findBoyugProgramDetailsWithBtu(boyugToUsers);

        model.addAttribute("details", details);
        model.addAttribute("users", users);
        return "userView/boyugMyPage/request-module";
    }

    @GetMapping(path = {"/boyug-response"})
    public String boyugResponse() {


        return "userView/boyugMyPage/boyug-response";
    }

    @GetMapping(path = {"/response-module"})
    public String responseModule(Model model, HttpServletRequest req,
                                 @RequestParam(defaultValue = "1", name = "pageNo") int pageNo) {
        // 보육원한테 신청이 들어온 내역
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebUserDetails userDetails = (WebUserDetails)  authentication.getPrincipal();

        boolean isHave = true;

        List<UserToBoyugDto> userToBoyugs = boyugMyPageService.findUserToBoyugs(userDetails.getUser().getUserId());

        if (userToBoyugs == null) {
            isHave = false;
        } else {
            // 페이징 처리
            int pageSize = 5;
            int pagerSize = 5;
            int dataCount = userToBoyugs.size();

            String uri = req.getRequestURI();
            String linkUrl = uri.substring(uri.lastIndexOf("/") + 1);
            String queryString = req.getQueryString();

            ProjectPager pager = new ProjectPager(dataCount, pageNo, pageSize, pagerSize, linkUrl, queryString);

            List<BoyugProgramDetailDto> pagingUserToBoyugs =
                    boyugMyPageService.findPagingUserToBoyugs(pageNo - 1, pageSize, userDetails.getUser().getUserId());
        }

        List<UserDto> users = boyugMyPageService.findPersonalUsers(userToBoyugs);
        List<BoyugProgramDetailDto> details = boyugMyPageService.findBoyugProgramDetails(userToBoyugs);

        model.addAttribute("users", users);
        model.addAttribute("details", details);

        return "userView/boyugMyPage/response-module";
    }

    @PostMapping(path = {"/response-confirm"})
    @ResponseBody
    public String responseConfirm(@RequestParam(name = "detailId") int detailId,
                                  @RequestParam(name = "userId") int userId) {

        boyugMyPageService.modifyUserToBoyug(detailId, userId);
        UserDto toUser = personalService.findUser(userId);

        if (toUser != null) {
            String message = "[Together] 신청하신 활동이 수락되었습니다. 내정보를 확인해주세요.";
            smsApi.notificationSend(toUser.getUserPhone(), message);
        }

        return "success";
    }

    @PostMapping(path = {"/response-refuse"})
    @ResponseBody
    public String responseRefuse(@RequestParam(name = "detailId") int detailId,
                                 @RequestParam(name = "userId") int userId) {

        boyugMyPageService.modifyUserToBoyugRefuse(detailId, userId);

        return "success";
    }

    @GetMapping(path = {"/boyug-matching"})
    public String boyugMatching(Model model) {


        return "userView/boyugMyPage/boyug-matching";
    }

    @GetMapping(path = {"/matching-module"})
    public String matchingModule(Model model, HttpServletRequest req,
                                 @RequestParam(defaultValue = "1", name = "pageNo") int pageNo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebUserDetails userDetails = (WebUserDetails)  authentication.getPrincipal();

        boolean isHave = true;

        List<UserToBoyugDto> userToBoyugs = boyugMyPageService.findMatchingUserToBoyugs(userDetails.getUser().getUserId());
        List<UserDto> users = boyugMyPageService.findPersonalUsers(userToBoyugs);
        List<BoyugProgramDetailDto> details = boyugMyPageService.findBoyugProgramDetails(userToBoyugs);

        List<BoyugToUserDto> boyugToUsers = boyugMyPageService.findConfirmBoyugToUsers(userDetails.getUser().getUserId());
        List<UserDto> btuUsers = boyugMyPageService.findPersonalUsersWithBtu(boyugToUsers);
        List<BoyugProgramDetailDto> btuDetails = boyugMyPageService.findBoyugProgramDetailsWithBtu(boyugToUsers);

        if (details.isEmpty() || btuDetails.isEmpty()) {
            isHave = false;
        } else {

            List<Matching> matchings = new ArrayList<>();
            for (int i = 0; i < details.size(); i++) {
                matchings.add(
                        new Matching(
                                users.get(i).getUserName(), details.get(i).getBoyugProgramDetailDate(),
                                details.get(i).getBoyugProgramDetailStartTime(), details.get(i).getBoyugProgramDetailEndTime(),
                                details.get(i).getSessionName()
                        )
                );
            }
            for (int i = 0; i < btuDetails.size(); i++) {
                matchings.add(
                        new Matching(
                                btuUsers.get(i).getUserName(), btuDetails.get(i).getBoyugProgramDetailDate(),
                                btuDetails.get(i).getBoyugProgramDetailStartTime(), btuDetails.get(i).getBoyugProgramDetailEndTime(),
                                btuDetails.get(i).getSessionName()
                        )
                );
            }

            Collections.sort(matchings, Comparator.comparing(Matching::getDate).reversed());

            // 페이징 처리
            int pageSize = 5;
            int pagerSize = 5;
            int dataCount = matchings.size();

            List<Matching> pagingMatchings =
                    matchings.stream().skip((long) (pageNo - 1) * pageSize).limit(pagerSize).toList();

            String uri = req.getRequestURI();
            String linkUrl = uri.substring(uri.lastIndexOf("/") + 1);
            String queryString = req.getQueryString();

            ProjectPager pager = new ProjectPager(dataCount, pageNo, pageSize, pagerSize, linkUrl, queryString);

            model.addAttribute("matchings", pagingMatchings);
            model.addAttribute("pager", pager);
            model.addAttribute("pageNo", pageNo);
            model.addAttribute("dataCount", dataCount);
        }

//        List<BoyugProgramDto> programs = boyugMyPageService.findMatchingBoyugToUsers(userDetails.getUser().getUserId());
//        List<BoyugToUserDto> boyugToUsers = boyugMyPageService.findMatchingBoyugToUsers2(programs);
//        System.out.println("programs : " + programs);
//        for (BoyugProgramDto programDto : programs) {
//            System.out.println("program : " + programDto.getBoyugProgramName());
//
//            for (BoyugProgramDetailDto detailDto : programDto.getProgramDetails()) {
//                System.out.println("detail : " + detailDto.getSessionName());
//
//                for (UserToBoyugDto utbDto : detailDto.getUserToBoyugs()) {
//                    System.out.println("UserToBoyug : " + utbDto.getUserId());
//                }
//            }
//
//        }

//        List<BoyugProgramDto> programs = boyugMyPageService.findBoyugProgramDetails2(userDetails.getUser().getUserId());

//        model.addAttribute("programs", programs);

        model.addAttribute("isHave", isHave);
        return "userView/boyugMyPage/matching-module";
    }

    @GetMapping(path = {"/boyug-my-session-list"})
    public String boyugMySessionList() {

        return "userView/boyugMyPage/boyug-my-session-list";
    }

    @GetMapping(path = {"/my-session-list-module"})
    public String mySessionList(Model model, HttpServletRequest req,
                                @RequestParam(defaultValue = "1", name = "pageNo") int pageNo) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebUserDetails userDetails = (WebUserDetails) authentication.getPrincipal();
        UserDto user = userDetails.getUser();

        boolean isHave = true;

        List<BoyugProgramDto> programs = boyugMyPageService.findBoyugPrograms(user.getUserId());

        if (programs == null) {
            isHave = false;
        } else {
            // 페이징 처리
            int pageSize = 5;
            int pagerSize = 5;
            int dataCount = programs.size();

            String uri = req.getRequestURI();
            String linkUrl = uri.substring(uri.lastIndexOf("/") + 1);
            String queryString = req.getQueryString();

            ProjectPager pager = new ProjectPager(dataCount, pageNo, pageSize, pagerSize, linkUrl, queryString);
            List<BoyugProgramDto> pagingPrograms = boyugMyPageService.findPagingBoyugPrograms(pageNo - 1, pageSize, user.getUserId());

            model.addAttribute("programs", pagingPrograms);
            model.addAttribute("pager", pager);
            model.addAttribute("pageNo", pageNo);
            model.addAttribute("dataCount", dataCount);
        }

        model.addAttribute("isHave", isHave);

        return "userView/boyugMyPage/my-session-list-module";
    }

    @GetMapping(path = {"/boyug-file-list"})
    public String boyugFileList() {

        return "userView/boyugMyPage/boyug-file-list";
    }

    @PostMapping(path = {"/modify-file"})
    @ResponseBody
    public String modifyFile(@RequestParam("attach") MultipartFile attach,
                             HttpServletRequest req) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebUserDetails userDetails = (WebUserDetails) authentication.getPrincipal();
        UserDto user = userDetails.getUser();

        ProfileImageDto userFile = new ProfileImageDto();
        userFile.setUserId(user.getUserId());

        if (!attach.isEmpty() && attach.getOriginalFilename().length() > 0) {
            try {
                String userFileName = attach.getOriginalFilename();
                String savedFileName = Util.makeUniqueFileName(userFileName);

                String dir = req.getServletContext().getRealPath("/profile-image");
                attach.transferTo(new File(dir, savedFileName)); // 파일 저장

                userFile.setImgOriginName(userFileName);
                userFile.setImgSavedName(savedFileName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                personalService.addProfileImage(userFile);
            } catch (Exception ex) {
                System.out.println("파일 등록 실패");
                ex.printStackTrace();
            }
        }

        return "success";
    }

    @GetMapping(path = {"/boyug-file-module"})
    public String boyugFileModule(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebUserDetails userDetails = (WebUserDetails) authentication.getPrincipal();
        UserDto user = userDetails.getUser();

        List<ProfileImageDto> images = boyugMyPageService.findProfileImages(user.getUserId());

        boolean isHave = true;

        if (images.isEmpty()) {
            isHave = false;
        } else {
            model.addAttribute("images", images);
        }

        model.addAttribute("isHave", isHave);
        return "userView/boyugMyPage/boyug-file-module";
    }

    @PostMapping(path = {"/remove-file"})
    @ResponseBody
    public String removeFile(@RequestParam(name = "imageId") int imageId) {

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        WebUserDetails userDetails = (WebUserDetails) authentication.getPrincipal();
//        UserDto user = userDetails.getUser();

//        System.out.println(imageId);
        boyugMyPageService.removeProfileImage(imageId);

        return "success";
    }

    @GetMapping(path = {"/boyug-my-info"})
    public String boyugMyInfo(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebUserDetails userDetails = (WebUserDetails) authentication.getPrincipal();

        UserDto user = personalService.findUser(userDetails.getUser().getUserId());
        BoyugUserDto userDetail = boyugMyPageService.findBoyugUser(user.getUserId());

        model.addAttribute("user", user);
        model.addAttribute("userDetail", userDetail);

//        System.out.println(user);
//        System.out.println(userDetail);

        return "userView/boyugMyPage/boyug-my-info";
    }

    @PostMapping(path = {"/send-email"})
    @ResponseBody
    public String sendEmail(@RequestParam(name = "userEmail") String userEmail, HttpServletRequest req,
                            HttpSession session) {

        int key = (int)(Math.random() * 8999) + 1000;

        // Session에 저장
        session.setAttribute(userEmail, key);
//        ServletContext ss = req.getServletContext();
//        ss.setAttribute(userEmail, key);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);  // 두번째 인자는 html 로 보낼 것인지, 일반 text 로 보낼 것인지

            messageHelper.setFrom("ddslk75@naver.com");
            messageHelper.setTo(new String[] {"ddslk75@naver.com", userEmail });
            message.setSubject("[Together] 이메일 인증번호");
            message.setContent(String.format("<html><body><h2>비밀번호 재설정 인증 메일</h2><br><br> 인증번호: %d </body></html>", key), "text/html;charset=utf-8");

            mailSender.send(message);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "success";
    }

    @PostMapping(path = {"/check-email"})
    @ResponseBody
    public boolean checkEmail(@RequestParam(name = "emailAuth") int emailAuth,
                              @RequestParam(name = "emailValue") String emailValue, HttpSession session) {
        boolean isCheck = false;

        int authValue = (Integer) session.getAttribute(emailValue);

        if (emailAuth == authValue) {
            isCheck = true;
        }

        return isCheck;
    }

    @PostMapping(path = {"/modify-boyug-info"})
    public String modifyBoyugInfo(UserDto user, BoyugUserDto boyugUser) {

        // 위도, 경도 추출
        Map<String, Object> xy = kaKaoApi.getKakaoSearch(user.getUserAddr2());
        user.setUserLongitude((String) xy.get("x"));
        user.setUserLatitude((String) xy.get("y"));

        boyugMyPageService.modifyBoyugUser(user, boyugUser);

        return "redirect:/myPage/boyug-my-info";
    }

}
