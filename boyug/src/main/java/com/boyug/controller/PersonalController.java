package com.boyug.controller;

import com.boyug.common.KaKaoApi;
import com.boyug.common.SmsApi;
import com.boyug.common.Util;
import com.boyug.dto.*;
import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.WebUserDetails;
import com.boyug.service.ActivityService;
import com.boyug.service.PersonalService;
import com.boyug.ui.ProjectPager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = {"/personal"})
@RequiredArgsConstructor
public class PersonalController {

    @Setter(onMethod_ = {@Autowired})
    private PersonalService personalService;

    @Setter(onMethod_ = {@Autowired})
    private ActivityService activityService;

    private final KaKaoApi kaKaoApi;

    @Value("${kakao.rest-api-key}")
    private String kakaoMapApiKey;

    @Setter(onMethod_ = { @Autowired })
    private SmsApi smsApi;

    @GetMapping(path = {"/personalHome"})
    public String personalHome(Model model) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        WebUserDetails userDetails = (WebUserDetails)  authentication.getPrincipal();
//
//        UserDetailDto personalUser = personalService.findUserDetail(userDetails.getUser().getUserId());
//
//        UserDto personal = personalService.findUser(userDetails.getUser().getUserId());
//
//        System.out.println(personal);
//        model.addAttribute("userDetail", personalUser);
//        model.addAttribute("user", personal);


        //////////////
        WebUserDetails userDetails = getUserDetails();

        UserDto personalUser = personalService.findUser(userDetails.getUser().getUserId());

        List<UserDto> users = personalService.findBoyugUsers();
        List<BoyugUserDto> boyugUsers = personalService.findBoyugUsersDetail();

        model.addAttribute("users", users);
        model.addAttribute("boyugUsers", boyugUsers);
        model.addAttribute("personalUser", personalUser);

        return "userView/personal/personalHome";
    }

    @GetMapping(path = {"/request-modal"})
    @ResponseBody
    public String requestModal(int index) {
        String result = "";
        switch (index) {
            case 0:
                result = "session-page";
                break;
            case 1:
                result = "/userView/activityPages/noinRegister";
                break;
            case 2:
                result = "personal-map";
                break;
            case 3:
                result = "myPage";
                break;
            case 5:
                result = "advice-module";
                break;
        }

        return result;
    }

    @GetMapping(path = {"/session-page"})
    public String sessionPage(@RequestParam(required = false) String sort, Model model) {

        // 로그인한 유저의 정보 갖고오기
        // String username = authentication.getName();
        WebUserDetails user = getUserDetails();
        int userId = user.getUser().getUserId();
        // 갖고 옴

        List<BoyugProgramDto> boyugProgramList = new ArrayList<>();

        if (sort == null) {
            // 5개 프로그램 갖고오기
            boyugProgramList = activityService.find5Program(userId);
        } else if (sort.equals("chltlstns")) {
            boyugProgramList = activityService.find5ProgramChltlstns(userId);
        } else {
            boyugProgramList = activityService.find5ProgramReRoll(userId);
        }



        for (BoyugProgramDto boyugProgram : boyugProgramList) {
            for (BoyugProgramDetailDto programDetail : boyugProgram.getProgramDetails()) {
                int detailId = programDetail.getBoyugProgramDetailId();
                boolean isApplied = activityService.isUserApplied(userId, detailId);

                programDetail.setApplied(isApplied);
            }
        }

        model.addAttribute("boyugProgramList", boyugProgramList);
        return "userView/personal/session-page";
    }

    @GetMapping(path = {"/myPage"})
    public String myPage(Model model) {
        WebUserDetails userDetails = getUserDetails();

        UserDetailDto personalUser = personalService.findUserDetail(userDetails.getUser().getUserId());

        UserDto personal = personalService.findUser(userDetails.getUser().getUserId());

        List<SessionDto> sessionList = personalService.findAllSession();

        List<BoyugProgramDetailDto> matchingProgramDetailAll = personalService.findConfirmBoyugProgramDetailAll(userDetails.getUser().getUserId());
        List<BoyugProgramDto> boyugPrograms = new ArrayList<>();

        for (BoyugProgramDetailDto detail : matchingProgramDetailAll) {
            BoyugProgramDto boyugProgram = personalService.findBoyugProgram(detail.getBoyugProgramId());

            if (boyugProgram != null) {
                boyugPrograms.add(boyugProgram);
            }
        }
        Set<Integer> seenIds = new HashSet<>();
        // 중복된 경우 리스트에서 제거
        boyugPrograms.removeIf(program -> !seenIds.add(program.getBoyugProgramId()));

//        System.out.println("personalUser : " + personalUser);
//        System.out.println("personal : " + personal);
        System.out.println("matching : " + matchingProgramDetailAll);
        model.addAttribute("userDetail", personalUser);
        model.addAttribute("user", personal);
        model.addAttribute("sessionList", sessionList);
        model.addAttribute("matchingList", matchingProgramDetailAll);
        return "userView/personal/myPage";
    }

    @GetMapping(path = {"/myPage/boyug-to-personal"})
    public String myPageSessionList(Model model, HttpServletRequest req,
                                    @RequestParam(defaultValue = "1", name = "pageNo") int pageNo) {
        WebUserDetails userDetails = getUserDetails();

        boolean isHave = true;

        List<BoyugProgramDetailDto> boyugProgramDetailAllList = personalService.findBoyugProgramDetailAll(userDetails.getUser().getUserId());

        if (boyugProgramDetailAllList == null) {
            isHave = false;
        } else {
            // 페이징 처리
            int pageSize = 5;
            int pagerSize = 5;
            int dataCount = boyugProgramDetailAllList.size();

            String uri = req.getRequestURI();
            String linkUrl = uri.substring(uri.lastIndexOf("/") + 1);
            String queryString = req.getQueryString();

            ProjectPager pager = new ProjectPager(dataCount, pageNo, pageSize, pagerSize, linkUrl, queryString);

            List<BoyugProgramDetailDto> pagingBoyugProgramDetails =
                    personalService.findPagingBoyugProgramDetailsInBTU(pageNo - 1, pageSize, userDetails.getUser().getUserId());

            List<BoyugProgramDto> boyugPrograms = new ArrayList<>();

            for (BoyugProgramDetailDto detail : pagingBoyugProgramDetails) {
                BoyugProgramDto boyugProgram = personalService.findBoyugProgram(detail.getBoyugProgramId());

                if (boyugProgram != null) {
                    boyugPrograms.add(boyugProgram);
                }
            }
            Set<Integer> seenIds = new HashSet<>();
            // 중복된 경우 리스트에서 제거
            boyugPrograms.removeIf(program -> !seenIds.add(program.getBoyugProgramId()));

            model.addAttribute("programDetails", pagingBoyugProgramDetails);
            model.addAttribute("programs", boyugPrograms);
            model.addAttribute("pager", pager);
            model.addAttribute("pageNo", pageNo);
            model.addAttribute("dataCount", dataCount);
        }

        model.addAttribute("isHave", isHave);
        return "userView/personal/boyug-to-personal";
    }

    @PostMapping(path = {"/myPage/request-confirm"})
    @ResponseBody
    public String requestConfirm(@RequestParam(name = "programDetailId") int programDetailId,
                                 @RequestParam(name = "isConfirm") boolean isConfirm) {

        WebUserDetails userDetails = getUserDetails();

        if (isConfirm) {
            personalService.modifyBoyugToUser(userDetails.getUser().getUserId(), programDetailId);
        } else {
            personalService.modifyBoyugToUserRefuse(userDetails.getUser().getUserId(), programDetailId);
        }


        return "success";
    }

    @GetMapping(path = {"/myPage/personal-to-boyug"})
    public String personalToBoyug(Model model, HttpServletRequest req,
                                  @RequestParam(defaultValue = "1", name = "pageNo") int pageNo) {
        WebUserDetails userDetails = getUserDetails();

        boolean isHave = true;

        List<BoyugProgramDetailDto> boyugProgramDetails = personalService.findBoyugProgramDetail(userDetails.getUser().getUserId());

        if (boyugProgramDetails == null) {
            isHave = false;
        } else {
            // 페이징 처리
            int pageSize = 5;
            int pagerSize = 5;

            int dataCount = boyugProgramDetails.size();

            String uri = req.getRequestURI();
            String linkUrl = uri.substring(uri.lastIndexOf("/") + 1);
            String queryString = req.getQueryString();

//            int start = pageSize * (pageNo - 1);

            ProjectPager pager = new ProjectPager(dataCount, pageNo, pageSize, pagerSize, linkUrl, queryString);

            List<BoyugProgramDetailDto> pagingBoyugProgramDetails = personalService.findPagingBoyugProgramDetails(pageNo - 1, pageSize);

            List<BoyugProgramDto> boyugPrograms = new ArrayList<>();

            for (BoyugProgramDetailDto detail : pagingBoyugProgramDetails) {
                BoyugProgramDto boyugProgram = personalService.findBoyugProgram(detail.getBoyugProgramId());

                if (boyugProgram != null) {
                    boyugPrograms.add(boyugProgram);
                }
            }

            Set<Integer> seenIds = new HashSet<>();
            // 중복된 경우 리스트에서 제거
            boyugPrograms.removeIf(program -> !seenIds.add(program.getBoyugProgramId()));

            model.addAttribute("programDetailList", pagingBoyugProgramDetails);
            model.addAttribute("boyugPrograms", boyugPrograms);
            model.addAttribute("pager", pager);
            model.addAttribute("pageNo", pageNo);
            model.addAttribute("dataCount", dataCount);
        }

        model.addAttribute("isHave", isHave);
        return "userView/personal/personal-to-boyug";
    }

    @GetMapping(path = {"/myPage/matching"})
    public String matching(Model model, HttpServletRequest req,
                           @RequestParam(defaultValue = "1", name = "pageNo") int pageNo) {

        WebUserDetails userDetails = getUserDetails();

        boolean isHave = true;

        List<BoyugProgramDetailDto> matchingProgramDetailAll = personalService.findConfirmBoyugProgramDetailAll(userDetails.getUser().getUserId());

        System.out.println(matchingProgramDetailAll);

        if (matchingProgramDetailAll == null) {
            isHave = false;
        } else {
            // 페이징 처리
            int pageSize = 5;
            int pagerSize = 5;
            int dataCount = matchingProgramDetailAll.size();

            List<BoyugProgramDetailDto> pagingMatchingDetails =
                    matchingProgramDetailAll.stream().skip((long) (pageNo - 1) * pageSize).limit(pagerSize).collect(Collectors.toList());

            String uri = req.getRequestURI();
            String linkUrl = uri.substring(uri.lastIndexOf("/") + 1);
            String queryString = req.getQueryString();
            ProjectPager pager = new ProjectPager(dataCount, pageNo, pageSize, pagerSize, linkUrl, queryString);

            model.addAttribute("matchingDetailList", pagingMatchingDetails);
            model.addAttribute("pager", pager);
            model.addAttribute("pageNo", pageNo);
            model.addAttribute("dataCount", dataCount);
        }

        model.addAttribute("isHave", isHave);

        return "userView/personal/matching";
    }

    @GetMapping(path = {"/personal-register"})
    public String personalLogin(HttpSession session, Model model) {
        // 화면에 출력해야함 id(지정) 데이터도 있음
        UserDto oAuth2User = (UserDto) session.getAttribute("oAuth2User");
        model.addAttribute("oAuth2User", oAuth2User);

        return "userView/personal/personal-register";
    }

    @PostMapping(path = {"/signup"})
    @ResponseBody
    public String singUp(String birthDay, UserDto userDto, UserDetailDto userDetailDto, String fileUrl,
                         @RequestParam("attach") MultipartFile attach, HttpServletRequest req) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            userDetailDto.setUserBirth(dateFormat.parse(birthDay));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 위도, 경도 추출
        Map<String, Object> xy = kaKaoApi.getKakaoSearch(userDto.getUserAddr2());
        userDto.setUserLongitude((String) xy.get("x"));
        userDto.setUserLatitude((String) xy.get("y"));

        int userId = personalService.addPersonalUser(userDto, userDetailDto);

        ProfileImageDto userFile = new ProfileImageDto();
        userFile.setUserId(userId);

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
        } else {
             // 카카오 프로필 이미지(url)일 경우 파일 다운+저장
            if(fileUrl != null || !fileUrl.equals("")) {
                downloadFile(userId, fileUrl, req);
            } else {
                System.out.println("프로필 저장 중 뭔가 잘못됐음");
            }
        }

        return "success";
    }

    @PostMapping(path = {"/send-sms"})
    @ResponseBody
    public boolean sendSms(String userPhone, HttpSession session) {

        boolean res = false;
        String result = personalService.findUserPhone(userPhone);

        res = result != null ? true : false;

        if (!res) {
            String code = smsApi.sendMessage(userPhone);
            session.setAttribute("smsCode", code);
        }

        return res;
    }

    @PostMapping(path = {"/modify-info"})
    @ResponseBody
    public String modifyInfo(String birthDay, UserDto user,
                             UserDetailDto userDetail,
                             @RequestParam(name="sessionItemName", required = false) List<String> sessionItemName,
                             @RequestParam(name="attach", required = false) MultipartFile attach,
                             @RequestParam(name = "attachId", required = false) Integer attachId,
                             HttpServletRequest req) {

        // 생년월일 dateFormat
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            userDetail.setUserBirth(dateFormat.parse(birthDay));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 위도, 경도 추출
        Map<String, Object> xy = kaKaoApi.getKakaoSearch(user.getUserAddr2());
        user.setUserLongitude((String) xy.get("x"));
        user.setUserLatitude((String) xy.get("y"));

        ProfileImageDto userFile = new ProfileImageDto();
        userFile.setUserId(user.getUserId());

        // 선호활동 insert to userDto
        if (sessionItemName != null) {
            Set<SessionDto> sessionDtos = new HashSet<>();
            for (String s : sessionItemName) {
                SessionDto sessionDto = personalService.findSessionId(s);
                sessionDtos.add(sessionDto);
            }
            user.setFavorites(sessionDtos);
        }

        if (!attach.isEmpty() && attach.getOriginalFilename().length() > 0) {
            ProfileImageDto profileImage = new ProfileImageDto();
            ArrayList<ProfileImageDto> profileImages = new ArrayList<>();
            try {
                String dir = req.getServletContext().getRealPath("/profile-image");
                String userFileName = attach.getOriginalFilename();
                String savedFileName = Util.makeUniqueFileName(userFileName);
                attach.transferTo(new File(dir, savedFileName));  // 파일 저장

                if (attachId != null) {
                    profileImage.setImageId(attachId);
                }
                profileImage.setUserId(user.getUserId());
                profileImage.setImgOriginName(userFileName);
                profileImage.setImgSavedName(savedFileName);
                profileImages.add(profileImage);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            user.setImages(profileImages);
        }
        System.out.println(user);
        personalService.modfiyUserAndUserDetail(user, userDetail);

        return "success";
    }

    @GetMapping(path = {"/personal-map"})
    public String personalMap(Model model) {
        WebUserDetails userDetails = getUserDetails();

        UserDto personalUser = personalService.findUser(userDetails.getUser().getUserId());

        List<UserDto> users = personalService.findBoyugUsers();
        List<BoyugUserDto> boyugUsers = personalService.findBoyugUsersDetail();

        model.addAttribute("users", users);
        model.addAttribute("boyugUsers", boyugUsers);
        model.addAttribute("personalUser", personalUser);

        return "userView/personal/personal-map";
    }

    @GetMapping(path = {"/advice-module"})
    public String adviceModule() {

        return "userView/personal/advice-module";
    }

    // 소셜 로그인 프로필 이미지 url 다운로드 함수
    public void downloadFile(int userId, String fileUrl, HttpServletRequest req) {
        ProfileImageDto userFile = new ProfileImageDto();
        String savedFileName = Util.makeUniqueFileName(fileUrl);

        userFile.setUserId(userId);
        userFile.setImgOriginName("kakao" + userId);
        userFile.setImgSavedName(savedFileName);

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // 응답 코드 체크
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 입력 스트림 생성
                InputStream inputStream = connection.getInputStream();
                // 파일 저장 경로 설정
                String targetPath = req.getServletContext().getRealPath("/profile-image");
                Path targetFilePath = Path.of(targetPath, savedFileName); // 경로, 파일이름
                // 파일 다운로드 및 저장
                Files.copy(inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                inputStream.close();
            } else {
                System.out.println("파일 다운 실패 response code: " + connection.getResponseCode());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            personalService.addProfileImage(userFile);
        } catch (Exception ex) {
            System.out.println("파일 등록 실패");
            ex.printStackTrace();
        }
    }


    // 로그인한 유저 정보
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
