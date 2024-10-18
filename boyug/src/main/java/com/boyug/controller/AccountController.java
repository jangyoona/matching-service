package com.boyug.controller;

import com.boyug.common.KaKaoApi;
import com.boyug.common.SmsApi;
import com.boyug.common.Util;
import com.boyug.dto.BoyugUserDto;
import com.boyug.dto.BoyugUserFileDto;
import com.boyug.dto.ProfileImageDto;
import com.boyug.dto.UserDto;
import com.boyug.service.AccountService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/userView/account")
@RequiredArgsConstructor
public class AccountController {

    @Setter(onMethod_ = { @Autowired })
    private AccountService accountService;

    private final KaKaoApi kakaoApi;

//    @Value("${kakao.rest-api-key}")
//    private String kakaoMapApiKey;

    @Setter(onMethod_ = { @Autowired })
    private SmsApi smsApi;

    @GetMapping("/privacy-policy")
    public String privacyPolicyShow() {
        return "userView/account/privacy-policy";
    }
    // 개인회원인지 보육원회원인지 선택 회원가입
    @GetMapping("/form")
    public String registerFormShow() {
        return "userView/account/form";
    }

    @GetMapping("/business-register")
    public String registerShow() {
        return "userView/account/business-register";
    }

    @GetMapping("/success")
    public String registerSuccessShow() {
        return "userView/account/success";
    }

    @GetMapping("/pending")
    public String pendingShow() {
        return "userView/account/pending";
    }

    @GetMapping("/reset-passwd")
    public String findPasswdShow() {
        return "userView/account/reset-passwd";
    }

    @GetMapping("/kakao-user")
    public String kakaoUserRegister(){
        return "userView/account/kakao-user";
    }

    // security kakao-login
    @GetMapping("/login/oauth2/code/kakao")
    public String kakaoOAuth2Login() {
        return "userView/account/login/oauth2/code/kakao";
    }

    @GetMapping("/send-message")
    @ResponseBody
    public String sandMessage(String userPhone, HttpSession session) {
        String code = smsApi.sendMessage(userPhone);
        if (code != null) {
            session.setAttribute("smsCode", code);
            return "success"; // 정상 전송 완료
        } else {
            return "error";
        }
    }

    @GetMapping("check-number")
    @ResponseBody
    public String checkNumber(String number, HttpSession session) {

        String code = (String) session.getAttribute("smsCode");

        if (number.equals(code)) {
            return "success";
        } else {
            return "error";
        }
    }

    @GetMapping("/login")
    public String loginShow(HttpServletRequest req, Model model) {
        String userName = "";
        Cookie[] cookies = req.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if (cookie.getName().equals("saveId")) {
                    userName = cookie.getValue();
                }
            }
        }
        model.addAttribute("saveId", userName);
        return "userView/account/login";
    }

    @GetMapping("/dup-check")
    @ResponseBody
    public Boolean idDupCheck(String userPhone) {

        long idDup = accountService.dupCheckUserPhone(userPhone);
        return idDup == 1;
    }

    @GetMapping("/socialId-check")
    @ResponseBody
    public boolean isSocialId(String userPhone) {
        int count = accountService.getSocialIdByUserPhone(userPhone);
        return count == 1;
    }

    @PostMapping("/business-register")
    public String register(UserDto user, BoyugUserDto boyugUser, String domain,
                           MultipartFile[] attach, MultipartFile attachProfile, HttpServletRequest req) {

        // 위도, 경도 추출
        Map<String, Object> xy = kakaoApi.getKakaoSearch(user.getUserAddr2());
        user.setUserLongitude((String) xy.get("x"));
        user.setUserLatitude((String) xy.get("y"));

        String email = boyugUser.getBoyugEmail().concat("@").concat(domain);
        boyugUser.setBoyugEmail(email);
        UserDto savedUser = accountService.registerBoyugUser(user, boyugUser);

        List<BoyugUserFileDto> boyugUserFileList = new ArrayList<>();
        ProfileImageDto profileImage = new ProfileImageDto();

        if (attach.length > 0 ) {
            try {
                // 업체 첨부파일
                String dir = req.getServletContext().getRealPath("/boyugUser-file");

                for (MultipartFile file : attach) {
                    String userFileName = file.getOriginalFilename();
                    String savedFileName = Util.makeUniqueFileName(userFileName);

                    BoyugUserFileDto userFile = new BoyugUserFileDto();
                    userFile.setUserId(savedUser.getUserId());
                    userFile.setFileOriginName(userFileName);
                    userFile.setFileSavedName(savedFileName);
                    file.transferTo(new File(dir, savedFileName)); // 파일 저장
                    boyugUserFileList.add(userFile);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (!attachProfile.isEmpty() && attachProfile.getOriginalFilename().length() > 0) {
                try {
                    // 프로필 사진
                    String profileUserFileName = attachProfile.getOriginalFilename();
                    String profileSavedFileName = Util.makeUniqueFileName(profileUserFileName);
                    String profileDir = req.getServletContext().getRealPath("/profile-image");

                    profileImage.setUser(savedUser);
                    profileImage.setImgOriginName(profileUserFileName);
                    profileImage.setImgSavedName(profileSavedFileName);
                    attachProfile.transferTo(new File(profileDir, profileSavedFileName)); // 파일 저장
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            try {
                accountService.insertAttachments(boyugUserFileList ,profileImage);
            } catch (Exception ex) {
                System.out.println("파일 등록 실패");
                ex.printStackTrace();
            }
        }
        return "redirect:/userView/account/success";
    }

    @PostMapping("/reset-passwd")
    @ResponseBody
    public boolean updatePasswd(String userPhone, String userPw) {
        if(userPw == null) {
            return false;
        }
        return accountService.updateUserPasswd(userPhone, userPw);
    }

    public void fileDownload(HttpServletRequest req) {
        try {
            // URL 객체 생성
            URL url = new URL("요청 url");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // 응답 코드 체크
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 입력 스트림 생성
                InputStream inputStream = connection.getInputStream();
                // 파일 저장 경로 설정
                String targetPath = req.getServletContext().getRealPath("/profile-image");
                String fileName = "파일명 지정"; // 저장할 파일 이름
                Path targetFilePath = Path.of(targetPath, fileName);
                // 파일 다운로드 및 저장
                Files.copy(inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                inputStream.close();
                System.out.println("File downloaded: " + targetFilePath);
            } else {
                System.out.println("Failed to download _HTTP response code: " + connection.getResponseCode());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //    @GetMapping("/logout/oauth/code/kakao")
//    public String kakaoLogout(HttpSession session) {
//        kakaoApi.kakaoLogout((String)session.getAttribute("kakaoToken"));
//        session.invalidate();
//        // 위에 메서드 내에서 responseCode 401 이면 오류 / 아니면 성공으로 if문 추가하고 여기서 반환값 받아서 결과에 따라 처리해도 좋을듯
//        return "redirect:/home";
//    }

    //    @GetMapping("/login/oauth/code/kakao")
//    public String kakaoLogin(String code, HttpSession session) {
//        // 1. 인가 코드 받기 (@RequestParam String code)
//
//        // 2. 토큰 받기
//        String accessToken = kakaoApi.getAccessToken(code);
//
//        // 3. 사용자 정보 받고 세션 저장
//        Map<String, Object> userInfo = kakaoApi.getUserInfo(accessToken);
//
//        KakaoLoginUserDto kakaoUser =
//                new KakaoLoginUserDto( (String)userInfo.get("id"),
//                                       (String)userInfo.get("connectedAt"),
//                                       (String)userInfo.get("nickname"),
//                                       (String) userInfo.get("profileImage") );
//
//        session.setAttribute("kakaoToken", accessToken);
//        session.setAttribute("kakaoUser", kakaoUser);
//
//        long idCheck = accountService.dupCheckUserPhone(kakaoUser.getId());
//        if(idCheck == 0) { // 해당 아이디로 사이트 회원가입 안되어 있으면 회원가입 창으로 이동
//            return "redirect:/userView/account/kakao-user";
//        }
//
//        // 카카오로그인 기반으로 회원가입 + 전화번호 인증 로직을 짜렴
//        // 카카오회원은 휴대폰번호 + 비밀번호는 토큰으로 해야될거 같은데
//        // 그리고 유저 테이블에 로그인 타입 컬럼도 넣어야 될듯
//        return "redirect:/home";
//    }
}
