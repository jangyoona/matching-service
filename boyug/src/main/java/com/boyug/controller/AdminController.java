package com.boyug.controller;

import com.boyug.common.Util;
import com.boyug.dto.*;
import com.boyug.entity.UserDetailEntity;
import com.boyug.entity.UserEntity;
import com.boyug.security.WebUserDetails;
import com.boyug.service.AdminService;
import com.boyug.service.BoardService;
import com.boyug.websocket.GroupwareWebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = {"/admin"})
public class AdminController {

    @Setter(onMethod_ = {@Autowired})
    private AdminService adminService;

    @Setter(onMethod_ = {@Autowired})
    private BoardService boardService;

    @Setter(onMethod_ = {@Autowired})
    private GroupwareWebSocketHandler socketHandler;

    @Value("${upload.path}") // application.properties 파일의 upload.path 속성의 값 injection
    private String uploadPath;

    @GetMapping(path = {"/home"})
    public String homeGet(HttpServletRequest req, Model model) throws JsonProcessingException  {

        // 프로그램수, 프로그램디테일수,개인 회원 수와 보육원 회원 수를 가져와서 모델에 추가
        int boyugProgramCount = adminService.getBoyugProgramCount();
        int boyugProgramDetailCount = adminService.getBoyugProgramDetailCount();
        int UserDetailCount = adminService.getUserDetailCount();
        int boyugUserCount = adminService.getBoyugUserCount();

        model.addAttribute("boyugProgramCount", boyugProgramCount);
        model.addAttribute("boyugProgramDetailCount", boyugProgramDetailCount);
        model.addAttribute("userDetailCount", UserDetailCount);
        model.addAttribute("boyugUserCount", boyugUserCount);

        List<UserDetailDto> userDetailDtos = adminService.findAllUser();

        // 연령별 회원 수를 저장할 Map
        Map<Integer, Long> ageCountMap = new HashMap<>();

        // 현재 날짜를 기준으로 연령 계산
        Calendar today = Calendar.getInstance();
        for (UserDetailDto dto : userDetailDtos) {
            if (dto.getUserBirth() != null) {
                Calendar birthDate = Calendar.getInstance();
                birthDate.setTime(dto.getUserBirth());
                int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

                // 생일이 지나지 않았으면 나이를 하나 줄임
                if (today.get(Calendar.MONTH) < birthDate.get(Calendar.MONTH) ||
                        (today.get(Calendar.MONTH) == birthDate.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < birthDate.get(Calendar.DAY_OF_MONTH))) {
                    age--;
                }

                // 연령별 회원 수 집계
                ageCountMap.put(age, ageCountMap.getOrDefault(age, 0L) + 1);
            }
        }

        // 연령대와 회원 수 리스트로 변환
        List<Integer> ages = new ArrayList<>(ageCountMap.keySet());
        List<Long> counts = new ArrayList<>();

        // 연령을 정렬
        Collections.sort(ages);

        // 정렬된 연령에 따라 카운트를 매핑
        for (Integer age : ages) {
            counts.add(ageCountMap.get(age));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String agesJson = objectMapper.writeValueAsString(ages);
        String countsJson = objectMapper.writeValueAsString(counts);

        model.addAttribute("ages", agesJson);
        model.addAttribute("counts", countsJson);

        return "/adminView/home";
    }

    @GetMapping(path = {"/administrator"})
    public String getAdministratorPage(@RequestParam(required = false) Integer userId, Model model) {
        if (userId == null) {
            userId = 5802;  // 기본값으로 가져올 유저 설정
        }

        // 유저와 그의 역할 정보를 가져와서 모델에 추가
        UserDto userDto = adminService.findUserWithRolesById(userId);
        model.addAttribute("user", userDto);

        boolean isAdmin = userDto.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName()));
        model.addAttribute("isAdmin", isAdmin);

        return "/adminView/administrator";
    }

    @PostMapping(path = {"/administrator"})
    public ResponseEntity<UserDto> PostAdministratorPage(@RequestParam(required = false) Integer userId, Model model) {

        // 유저와 그의 역할 정보를 가져와서 모델에 추가
        UserDto userDto = adminService.findUserWithRolesById(userId);
        model.addAttribute("user", userDto);

        boolean isAdmin = userDto.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName()));
        model.addAttribute("isAdmin", isAdmin);

        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/toggleAdmin")
    @ResponseBody
    public ResponseEntity<Map<String, String>> toggleAdmin(@RequestParam Integer userId, @RequestParam String action) {
        String message;

        if ("add".equals(action)) {
            // 관리자 권한 추가 로직
            adminService.addRoleToUser(userId, "ROLE_ADMIN");
            message = "관리자 권한이 추가되었습니다.";
        } else {
            // 관리자 권한 제거 로직
            adminService.removeRoleFromUser(userId, "ROLE_ADMIN");
            message = "관리자 권한이 제거되었습니다.";
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", message);

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = {"/session"})
    public String sessionGet(Model model) {
        List<SessionDto> sessionDtoList = adminService.findAllSession();
        model.addAttribute("sessionList", sessionDtoList);
        List<UserSessionDto> userSessionDtoList = adminService.findAllUserSession();
        model.addAttribute("userSessionList", userSessionDtoList);
        return "/adminView/session";
    }

    @DeleteMapping("/remove/{sessionId}")
    public ResponseEntity<Void> removeSession(@PathVariable int sessionId) {
        adminService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/addsession")
    @ResponseBody
    public ResponseEntity<String> addSession(@RequestBody SessionDto sessionDto) {
        try {
            adminService.addSession(sessionDto.toEntity());
            return ResponseEntity.ok("세션이 추가되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("세션 추가 실패: " + e.getMessage());
        }
    }
    @PutMapping("/updateSessionActive/{sessionId}")
    public ResponseEntity<Void> updateSessionActive(@PathVariable int sessionId, @RequestBody Map<String, Boolean> request) {
        boolean isActive = request.get("active");
        adminService.updateSessionActive(sessionId, isActive);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/updateSessionName/{sessionId}")
    public ResponseEntity<Void> updateSessionName(@PathVariable int sessionId, @RequestBody Map<String, String> payload) {
        String newSessionName = payload.get("sessionName");
        // 세션 이름을 업데이트하는 로직
        adminService.updateSessionName(sessionId, newSessionName);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/updateUserSessionActive/{userSessionId}")
    public ResponseEntity<Void> updateUserSessionActive(@PathVariable int userSessionId, @RequestBody Map<String, Boolean> request) {
        boolean isActive = request.get("active");
        adminService.updateUserSessionActive(userSessionId, isActive);
        return ResponseEntity.ok().build();
    }





    @GetMapping(path = {"/index2"})
    public String index2Get(HttpServletRequest req, Model model) {

        return "/adminView/index2";
    }

    @GetMapping(path = {"/statistics"})
    public String statisticsGet(HttpServletRequest req, Model model) throws JsonProcessingException {
        List<BoyugProgramDto> boyugProgramDtos = adminService.getAllBoyugPrograms();

        // 날짜 기준으로 정렬
        boyugProgramDtos.sort(Comparator.comparing(BoyugProgramDto::getBoyugProgramRegdate));

        List<String> dates = new ArrayList<>();
        List<String> programNames = new ArrayList<>();
        List<Integer> programCounts = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // 원하는 날짜 형식으로 설정

        for (BoyugProgramDto dto : boyugProgramDtos) {
            dates.add(dateFormat.format(dto.getBoyugProgramRegdate())); // 날짜 형식 변환
            programNames.add(dto.getBoyugProgramName());
            programCounts.add(dto.getBoyugProgramCount());
        }
        // JSON 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String datesJson = objectMapper.writeValueAsString(dates);
        String programNamesJson = objectMapper.writeValueAsString(programNames);
        String programCountsJson = objectMapper.writeValueAsString(programCounts);

        model.addAttribute("dates", datesJson);
        model.addAttribute("programNames", programNamesJson);
        model.addAttribute("programCounts", programCountsJson);

        return "/adminView/statistics";
    }

    @GetMapping("/statistics4")
    public String getProgramsByDate(HttpServletRequest req, Model model) throws JsonProcessingException {
        List<Object[]> statistics = adminService.getStatisticsByDate();

        Map<String, Integer> dateCountMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // 날짜 형식 설정

        // 날짜별 카운트 집계
        for (Object[] result : statistics) {
            Date regDate = (Date) result[0];
            Long count = (Long) result[1];
            String dateString = dateFormat.format(regDate);

            dateCountMap.put(dateString, dateCountMap.getOrDefault(dateString, 0) + count.intValue());
        }

        // 날짜를 정렬하여 리스트 생성
        List<String> sortedDates = new ArrayList<>(dateCountMap.keySet());
        Collections.sort(sortedDates); // 날짜 문자열 정렬

        // 정렬된 날짜에 따른 카운트 리스트 생성
        List<Integer> sortedCounts = new ArrayList<>();
        for (String date : sortedDates) {
            sortedCounts.add(dateCountMap.get(date)); // 정렬된 날짜에 맞는 카운트 추가
        }

        // JSON 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String datesJson = objectMapper.writeValueAsString(sortedDates);
        String countsJson = objectMapper.writeValueAsString(sortedCounts);

        model.addAttribute("dates", datesJson);
        model.addAttribute("counts", countsJson);

        return "/adminView/statistics4";
    }

    @GetMapping("/statistics5")
    public String getProgramDetailsByDate(HttpServletRequest req, Model model) throws JsonProcessingException {
        List<Object[]> statistics = adminService.getDetailsByDate();

        Map<String, Integer> dateCountMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 날짜와 카운트 수집
        for (Object[] result : statistics) {
            LocalDate regDate = (LocalDate) result[0];
            Long count = (Long) result[1];
            String dateString = dateFormat.format(Date.from(regDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            dateCountMap.put(dateString, dateCountMap.getOrDefault(dateString, 0) + count.intValue());
        }

        // 날짜와 카운트 리스트 생성
        List<Map.Entry<String, Integer>> dateCountList = new ArrayList<>(dateCountMap.entrySet());

        // 날짜순으로 정렬
        dateCountList.sort((entry1, entry2) -> {
            LocalDate date1 = LocalDate.parse(entry1.getKey());
            LocalDate date2 = LocalDate.parse(entry2.getKey());
            return date1.compareTo(date2);
        });

        // 정렬된 날짜와 카운트 분리
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dateCountList) {
            dates.add(entry.getKey());
            counts.add(entry.getValue());
        }

        // JSON 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String datesJson = objectMapper.writeValueAsString(dates);
        String countsJson = objectMapper.writeValueAsString(counts);

        model.addAttribute("dates", datesJson);
        model.addAttribute("counts", countsJson);
        return "/adminView/statistics5";
    }

    @GetMapping(path = {"/statistics2"})
    public String statistics2Get(HttpServletRequest req, Model model) throws JsonProcessingException {
        List<UserDetailDto> userDetailDtos = adminService.findAllUser();

        // 연령별 회원 수를 저장할 Map
        Map<Integer, Long> ageCountMap = new HashMap<>();

        // 현재 날짜를 기준으로 연령 계산
        Calendar today = Calendar.getInstance();
        for (UserDetailDto dto : userDetailDtos) {
            if (dto.getUserBirth() != null) {
                Calendar birthDate = Calendar.getInstance();
                birthDate.setTime(dto.getUserBirth());
                int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

                // 생일이 지나지 않았으면 나이를 하나 줄임
                if (today.get(Calendar.MONTH) < birthDate.get(Calendar.MONTH) ||
                        (today.get(Calendar.MONTH) == birthDate.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < birthDate.get(Calendar.DAY_OF_MONTH))) {
                    age--;
                }

                // 연령별 회원 수 집계
                ageCountMap.put(age, ageCountMap.getOrDefault(age, 0L) + 1);
            }
        }

        // 연령대와 회원 수 리스트로 변환
        List<Integer> ages = new ArrayList<>(ageCountMap.keySet());
        List<Long> counts = new ArrayList<>();

        // 연령을 정렬
        Collections.sort(ages);

        // 정렬된 연령에 따라 카운트를 매핑
        for (Integer age : ages) {
            counts.add(ageCountMap.get(age));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String agesJson = objectMapper.writeValueAsString(ages);
        String countsJson = objectMapper.writeValueAsString(counts);

        model.addAttribute("ages", agesJson);
        model.addAttribute("counts", countsJson);

        return "/adminView/statistics2";
    }

    @GetMapping("/statistics3")
    public String userCountBySession(Model model) {
        List<Object[]> userCounts = adminService.getUserCountBySession();

        // 세션 ID와 회원 수를 모델에 추가
        model.addAttribute("userCounts", userCounts);
        return "/adminView/statistics3";
    }

    @GetMapping(path = {"/userlist"})
    public String userlistGet(HttpServletRequest req, Model model) {

        List<UserDetailDto> users = adminService.findAllUser();
		model.addAttribute("userList", users);
        List<BoyugUserDto> boyugs = adminService.findAllBoyug();
		model.addAttribute("boyugList", boyugs);
        return "/adminView/userlist";
    }

    @GetMapping(path = {"/inquirylist"})
    public String inquirylistGet(Model model) {
        List<BoardDto> notices = boardService.findAllNotice();
        model.addAttribute("notices", notices);
        List<BoardDto> boardDtos = boardService.findAllBoard();
        model.addAttribute("boardList", boardDtos);
        return "/adminView/inquirylist";
    }

    @GetMapping(path = {"/inquiryDelete"})
    public String inquiryDeleteGet(int boardId, Model model) {
        boardService.deleteBoard(boardId);
        return "redirect:/admin/inquirylist";
    }

    @GetMapping(path = {"/inquiryEdit"})
    public String inquiryEditGet(int boardId, Model model) {

        BoardDto boardDto = boardService.findBoardByBoardId(boardId);
        model.addAttribute("board", boardDto);

        return "/adminView/inquiryEdit";
    }
    @PostMapping(path = {"/inquiryEdit"})
    public String inquiryEditPost(BoardDto boardDto, MultipartFile attach, HttpServletRequest req) {
        // 첨부파일 있을경우
        if (attach != null && attach.getSize() > 0) {
            BoardAttachDto attachment = new BoardAttachDto();
            ArrayList<BoardAttachDto> attachments = new ArrayList<>();
            try {
                String dir = req.getServletContext().getRealPath("/board-attachments");
                String userFileName = attach.getOriginalFilename();
                String savedFileName = Util.makeUniqueFileName(userFileName);
                attach.transferTo(new File(dir, savedFileName)); // 파일 저장

                attachment.setBoardId(boardDto.getBoardId());
                attachment.setBoardAttachOriginName(userFileName);
                attachment.setBoardAttachSaveName(savedFileName);
                attachments.add(attachment);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            boardDto.setAttachments(attachments);
        }

        try {
            boardService.modifyBoard(boardDto);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/admin/inquirylist";
        }

//        boardService.editinquiryboard(boardDto);
        return "redirect:/admin/inquirylist";
    }

    @GetMapping(path = {"/delete-attach"})
    @ResponseBody
    public ResponseEntity<String> deleteAttach(@RequestParam(required = false) Integer attachNo, HttpServletRequest req) {
        if (attachNo == null) {
            return ResponseEntity.badRequest().body("Invalid attachNo");
        }

        BoardAttachDto attach = boardService.findBoardAttachByAttachNo(attachNo);
        if (attach == null) {
            return ResponseEntity.notFound().build();
        }

        String dirPath = req.getServletContext().getRealPath("/board-attachments");
        File file = new File(dirPath, attach.getBoardAttachSaveName());

        // 파일 삭제
        if (file.exists() && !file.delete()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete file");
        }

        // DB에서 삭제
        try {
            boardService.deleteBoardAttach(attachNo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete attachment from database");
        }

        return ResponseEntity.ok("success");
    }

    // 공지글 작성 페이지로 이동
    @GetMapping("/noticewrite")
    public String noticeWriteGet(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebUserDetails userDetails = (WebUserDetails)  authentication.getPrincipal();
        model.addAttribute("userId", userDetails.getUser().getUserId());
        model.addAttribute("username", userDetails.getUsername());
        return "/adminView/noticeWrite";
    }

    // 공지글 작성 처리
    @PostMapping("/noticewrite")
    public String noticeWritePost(@ModelAttribute("board") BoardDto boardDto,
                                MultipartFile attach,HttpServletRequest req) {
        ArrayList<BoardAttachDto> attachments = new ArrayList<>();
        if (!attach.isEmpty() && attach.getOriginalFilename().length() > 0) {
            BoardAttachDto attachment = new BoardAttachDto();
            try {
                String userFileName = attach.getOriginalFilename();
                String savedFileName = Util.makeUniqueFileName(userFileName);

                // static 경로에 파일 저장 (예: /src/main/resources/static/uploads)
                String uploadDir = req.getServletContext().getRealPath("/board-attachments");
                File uploadDirectory = new File(uploadDir);
                if (!uploadDirectory.exists()) {
                    uploadDirectory.mkdirs(); // 디렉토리가 없으면 생성
                }

                // 파일 저장
                attach.transferTo(new File(uploadDirectory, savedFileName));

                attachment.setBoardAttachOriginName(userFileName);
                attachment.setBoardAttachSaveName(savedFileName);
                attachments.add(attachment);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        boardDto.setAttachments(attachments);

        // boardCategory 값을 5로(공지사항) 설정
        boardDto.setBoardCategory(5);
        try {
            boardService.writeBoard(boardDto);
        } catch (Exception ex) {
            System.out.println("글쓰기 실패");
        }
        sendMessages();
        return "redirect:/admin/inquirylist"; // 작성 후 리스트 페이지로 리다이렉트
    }

    @GetMapping(path = {"/sangdam"})
    public String sangdamGet(Model model) {
        List<SangdamDto> sangdamDtoList = adminService.findAllSangdam();
        model.addAttribute("sangdamList", sangdamDtoList);
        return "/adminView/sangdam";
    }
    @PostMapping("/updateActive")
    @ResponseBody
    public String updateSangdamActive(@RequestParam int sangdamId, @RequestParam String sangdamActive) {
        try {
            adminService.updateSangdamActive(sangdamId, sangdamActive);
            return "success";
        } catch (Exception e) {
            return "failure";
        }
    }


    @GetMapping(path = {"/userboyugprogram"})
    public String userboyugprogramGet(@RequestParam(required = false) Integer userId, Model model) {

        if (userId == null) {
            userId = 2802; // 적절한 기본값
        }
        BoyugUserDto boyuguser = adminService.getBoyugUserById(userId);
        model.addAttribute("boyuguser", boyuguser);

        // BoyugProgramDto 리스트를 가져와서 model에 추가
        List<BoyugProgramDto> boyugProgramDtos = adminService.getBoyugProgramsByUserId(userId);
        model.addAttribute("boyugProgramDtos", boyugProgramDtos);

        return "/adminView/userboyugprogram";
    }

    @PostMapping(path = {"/userboyugprogram"})
    public String userboyugprogramPost(@RequestParam(required = false) Integer userId, Model model) {
        // 회원 정보 가져오기
        BoyugUserDto boyuguser = adminService.getBoyugUserById(userId);
        model.addAttribute("boyuguser", boyuguser);

        // BoyugProgramDto 리스트를 가져와서 model에 추가
        List<BoyugProgramDto> boyugProgramDtos = adminService.getBoyugProgramsByUserId(userId);
        model.addAttribute("boyugProgramDtos", boyugProgramDtos);

        // 전체 영역을 포함한 fragment 반환
        return "adminView/userboyugprogram :: userInfoAndTable";
    }

    @GetMapping(path = {"/userdo"})
    public String userDetailProgramDetailGet(@RequestParam(required = false) Integer userId, Model model) {

        if (userId == null) {
            userId = 4152; // 적절한 기본값
        }

        UserDetailDto userDetailDto =  adminService.findUserDetailById(userId);
        model.addAttribute("user", userDetailDto );

        // UserToBoyugDto 리스트를 가져와서 model에 추가
        List<UserToBoyugDto> Dtos = adminService.findUserToBoyugByUserId(userId);
        model.addAttribute("userToBoyugs", Dtos);

        return "/adminView/userdo";
    }

    @PostMapping(path = {"/userdo"})
    public String userDetailProgramDetailPost(@RequestParam(required = false) Integer userId, Model model) {
        // 회원 정보 가져오기
        UserDetailDto userDetailDto =  adminService.findUserDetailById(userId);
        model.addAttribute("user", userDetailDto );

        // UserToBoyugDto 리스트를 가져와서 model에 추가
        List<UserToBoyugDto> Dtos = adminService.findUserToBoyugByUserId(userId);
        model.addAttribute("userToBoyugs", Dtos);

        // 전체 영역을 포함한 fragment 반환
        return "adminView/userdo :: userInfoAndTable";
    }


    @ResponseBody
    @PostMapping(path = {"/boyug-confirm"})
    public  String boyug_confirm(@RequestParam int userId) {
        try {
            adminService.boyugcomfirm(userId);
            return "success";
        } catch (RuntimeException e) {
            return "failure";
        }
    }

    @ResponseBody
    @PostMapping("/update-healthstatus")
    public String updateHealthStatus(@RequestParam int userId, @RequestParam String userHealth) {
        try {
            adminService.updateHealthStatus(userId, userHealth);
            return "success";
        } catch (Exception e) {
            return "failure";
        }
    }

    @ResponseBody
    @PostMapping("/update-protector-phone")
    public String updateProtectorPhone(@RequestParam int userId, @RequestParam String protectorPhone) {
//        // 보호자 연락처가 '0'인 경우에만 업데이트를 진행
//        if ("0".equals(protectorPhone)) {
//            return "failure";  // 보호자 연락처가 '0'이면 업데이트 실패
//        }

        try {
            // 서비스에서 보호자 연락처 업데이트 로직 호출
            adminService.updateProtectorPhone(userId, protectorPhone);
            return "success";
        } catch (Exception e) {
            return "failure";
        }
    }

    @PostMapping("/update-gender")
    @ResponseBody
    public String updateUserGender(@RequestParam int userId, @RequestParam String userGender) {
        try {
            // userId에 해당하는 유저 정보 가져오기
            UserDetailEntity user = adminService.findById(userId);

            // 성별 업데이트
            user.setUserGender(userGender);
            adminService.save(user); // 업데이트된 정보 저장

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @ResponseBody
    @PostMapping("/update-username")
    public String updateUserName(@RequestParam int userId, @RequestParam String userName) {
        try {
            // userService를 통해 사용자 이름 업데이트
            boolean isUpdated = adminService.updateUserName(userId, userName);

            if (isUpdated) {
                return "success"; // 성공 시 반환
            } else {
                return "fail"; // 업데이트 실패 시 반환
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error"; // 예외 발생 시 반환
        }
    }

    @ResponseBody
    @PostMapping("/update-useractive")
    public String updateUserActive(@RequestParam int userId, @RequestParam boolean userActive) {
        try {
            // userId로 회원 정보를 조회하고 userActive 값을 업데이트
            UserEntity user = adminService.findByUserId(userId);  // userRepository에서 userId로 사용자 조회

            if (user != null) {
                user.setUserActive(userActive);  // userActive 값 업데이트
                adminService.saveuser(user);  // 변경 사항 저장
                return "success";
            } else {
                return "fail";  // 사용자를 찾지 못한 경우
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "error";  // 예외가 발생한 경우
        }
    }

    @ResponseBody
    @PostMapping("/update-boyug-childnum")
    public String updateBoyugChildNum(@RequestParam int userId, @RequestParam int boyugChildNum) {

        try {
            // 서비스에서 아이수 업데이트 로직 호출
            adminService.updateBoyugChildNum(userId, boyugChildNum);
            return "success";
        } catch (Exception e) {
            return "failure";
        }
    }

    @ResponseBody
    @PostMapping("/update-boyug-email")
    public String updateBoyugEmail(@RequestParam int userId, @RequestParam String boyugEmail) {

        try {
            adminService.updateBoyugEmail(userId, boyugEmail);
            return "success";
        } catch (Exception e) {
            return "failure";
        }
    }

    @ResponseBody
    @PostMapping("/update-boyug-name")
    public String updateBoyugName(@RequestParam int userId, @RequestParam String boyugName) {

        try {
            adminService.updateBoyugName(userId, boyugName);
            return "success";
        } catch (Exception e) {
            return "failure";
        }
    }

    @ResponseBody
    @PostMapping("/updateUserBirth")
    public ResponseEntity<String> updateUserBirth(
            @RequestParam int userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date userBirth) {

        adminService.updateUserBirth(userId, userBirth);
        return ResponseEntity.ok("success");
    }


    public void sendMessages() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        String message = "WebSocket event - " + sdf.format(new Date());
        // String message = "WebSocket event - " + Math.ceil(Math.random() * 100);
        try {
            // socketHandler.sendMessageToAll(message);
            socketHandler.sendMessageToSomeone(message, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping(path = "/updateProgramStatus")
    public ResponseEntity<String> updateProgramStatusPost(@RequestParam int boyugProgramId, @RequestParam boolean boyugProgramActive) {
        // 프로그램 상세 활성 상태 업데이트 로직
        adminService.updateProgramStatus(boyugProgramId, boyugProgramActive);
        return ResponseEntity.ok("Success");
    }
    @PostMapping(path = "/updateProgramDetailStatus")
    public ResponseEntity<String> updateProgramDetailStatus(@RequestParam int boyugProgramDetailId, @RequestParam boolean boyugProgramDetailActive) {
        // 프로그램 상세 활성 상태 업데이트 로직
        adminService.updateProgramDetailStatus(boyugProgramDetailId, boyugProgramDetailActive);
        return ResponseEntity.ok("Success");
    }
}
