package com.boyug.controller;

import com.boyug.common.Util;
import com.boyug.dto.BoardAttachDto;
import com.boyug.dto.BoardDto;
import com.boyug.dto.UserDto;
import com.boyug.security.WebUserDetails;
import com.boyug.service.BoardService;
import com.boyug.ui.ProjectPager;
import com.boyug.ui.ThePager;
import com.boyug.view.DownloadView1;
import com.boyug.websocket.GroupwareWebSocketHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping(path = {"/board"})
public class BoardController {

    @Setter(onMethod_ = {@Autowired})
    private BoardService boardService;

    @Setter(onMethod_ = {@Autowired})
    private GroupwareWebSocketHandler socketHandler;

    @Value("${upload.path}") // application.properties 파일의 upload.path 속성의 값 injection
    private String uploadPath;

    @GetMapping(path = {"/list"})
    public String listRange(@RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                            HttpServletRequest req, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebUserDetails userDetails = (WebUserDetails)  authentication.getPrincipal();
        model.addAttribute("userId", userDetails.getUser().getUserId());
        int userId = userDetails.getUser().getUserId();
        int pageSize = 5;        // 한 페이지에 표시하는 데이터 갯수
        int pagerSize = 5;        // 한 번에 표시되는 페이지 번호 갯수
//        int dataCount = boardService.getBoardCounter();    // 전체 데이터 갯수
        int dataCount = boardService.getBoardCountByUserId(userId);    // 특정 userId의 데이터 갯수
        String uri = req.getRequestURI();
        String linkUrl = uri.substring(uri.lastIndexOf("/") + 1); // 페이지 번호를 클릭했을 때 요청을 보낼 URL ( 목록 페이지 경로 )
        String queryString = req.getQueryString();

        int start = pageSize * (pageNo - 1) + 1; // 현재 페이지의 첫번째 데이터 행 번호

        ThePager pager = new ThePager(dataCount, pageNo, pageSize, pagerSize, linkUrl, queryString);

        List<BoardDto> notices = boardService.findBoardByNotice();
        List<BoardDto> boards = boardService.findBoardByRangeUserId(pageNo - 1, pageSize, userId);

        model.addAttribute("noticeList", notices);
        model.addAttribute("boardList", boards); // Model 타입 전달인자에 데이터 저장 -> View(JSP)로 전달
        model.addAttribute("pager", pager);
        model.addAttribute("pageNo", pageNo);

        return "/userView/board/list";    //  board/list
    }

    @GetMapping(path = {"/write"})
    public String writeForm(HttpSession session,Model model) {
        //로그인 안했을시 로그인화면으로
//        UserDto loginUser = (UserDto) session.getAttribute("loginuser");
//        if (loginUser == null) {
//            return "redirect:/userView/account/login";
//        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebUserDetails userDetails = (WebUserDetails)  authentication.getPrincipal();
        model.addAttribute("userId", userDetails.getUser().getUserId());
        model.addAttribute("username", userDetails.getUsername());
        return "/userView/board/write";
    }

    @PostMapping(path = {"/write"})
    public String write(@ModelAttribute("board") BoardDto board,
                        MultipartFile attach, // <input type="file"의 데이터를 담은 변수
                        HttpServletRequest req) {

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
        board.setAttachments(attachments);

        try {
            boardService.writeBoard(board);
        } catch (Exception ex) {
            System.out.println("글쓰기 실패");
        }
        sendMessages();
        return "redirect:/board/list";
    }

//    @PostMapping(path = {"/write"})
//    public String write(@ModelAttribute("board") BoardDto board,
//                        MultipartFile attach, // <input type="file"의 데이터를 담은 변수
//                        HttpServletRequest req) {
//
//        // 데이터 읽기 : 컨트롤러 메서드의 전달인자를 통해 자동으로 데이터 읽기 실행
//        // 데이터 처리 : 서비스 호출
//        ArrayList<BoardAttachDto> attachments = new ArrayList<>();
//        if (!attach.isEmpty() && attach.getOriginalFilename().length() > 0) {
//            BoardAttachDto attachment = new BoardAttachDto();
//            try {
//                String userFileName = attach.getOriginalFilename();
//                String savedFileName = Util.makeUniqueFileName(userFileName);
//
//                attach.transferTo(new File(uploadPath, savedFileName)); // 파일 저장
//
//                attachment.setBoardAttachOriginName(userFileName);
//                attachment.setBoardAttachSaveName(savedFileName);
//                attachments.add(attachment);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//        board.setAttachments(attachments);
//
//        try {
//            boardService.writeBoard(board);
//        } catch (Exception ex) {
//            System.out.println("글쓰기 실패");
//        }
//
//        sendMessages();
//
//        // 이동
//        return "redirect:/board/list";
//    }

    @GetMapping(path = {"/detail"}) // 주소?d1=v1&d2=v2
    public String detailWithQueryString(
            @RequestParam(value = "boardId", required = false) Integer boardId,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            HttpSession session, Model model) {

        if (boardId == null) { // 요청 데이터의 값이 없는 경우
            return "redirect:/board/list";
        }

        BoardDto board = boardService.findBoardByBoardId(boardId);
//        UserDto loginUser = (UserDto) session.getAttribute("loginuser");
        // 비로그인이거나, userId가 다르면 리스트로
//        if (loginUser == null || !Objects.equals(loginUser.getUserId(), board.getUserId())) {
//            return "redirect:/board/list";
//        }

        model.addAttribute("board", board);
        model.addAttribute("pageNo", pageNo);
        model.addAttribute("enter", "\n");

        return "/userView/board/detail";
    }
    @GetMapping(path = {"/notice"}) // 주소?d1=v1&d2=v2
    public String noticeWithQueryString(
            @RequestParam(value = "boardId", required = false) Integer boardId,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            HttpSession session, Model model) {

        if (boardId == null) { // 요청 데이터의 값이 없는 경우
            return "redirect:/board/list";
        }

        BoardDto board = boardService.findBoardByBoardId(boardId);
//        UserDto loginUser = (UserDto) session.getAttribute("loginuser");
        // 비로그인이거나, userId가 다르면 리스트로
//        if (loginUser == null || !Objects.equals(loginUser.getUserId(), board.getUserId())) {
//            return "redirect:/board/list";
//        }

        model.addAttribute("board", board);
        model.addAttribute("pageNo", pageNo);
        model.addAttribute("enter", "\n");

        return "/userView/board/notice";
    }

    @GetMapping(path = {"/detail/{boardId}"}) // 주소/data
    public String detailWithPathVariable(@PathVariable("boardId") Integer boardId, Model model) {

        BoardDto board = boardService.findBoardByBoardId(boardId);
        model.addAttribute("board", board);

        return "/userView/board/detail";
    }

    @GetMapping(path = {"/download"})
    public View downloadWithQueryString(@RequestParam("attachno") int attachNo, Model model) {

        BoardAttachDto boardAttach = boardService.findBoardAttachByAttachNo(attachNo);

        model.addAttribute("attach", boardAttach); // View에서 사용하도록 데이터 전달
        model.addAttribute("uploadPath", uploadPath);

        // 다운로드 처리 -> 사용자 정의 View 사용
        return new DownloadView1();
        // return new DownloadView2();
    }

    @GetMapping(path = {"/delete"})
    public String delete(int boardId, @RequestParam(defaultValue = "-1") int pageNo) {

        if (pageNo == -1) {
            return "redirect:/board/list";
        }

        boardService.deleteBoard(boardId);

        return String.format("redirect:/board/list?pageNo=%d", pageNo);

    }

    @GetMapping(path = {"/edit"})
    public String showEditForm(int boardId, @RequestParam(defaultValue = "-1") int pageNo, Model model) {

        if (pageNo == -1) {
            return "redirect:/board/list";
        }

        BoardDto board = boardService.findBoardByBoardId(boardId);
        model.addAttribute("board", board);
        model.addAttribute("pageNo", pageNo);

        return "/userView/board/edit";
    }

    @PostMapping(path = {"/edit"})
    public String editBoard(BoardDto board, MultipartFile attach, HttpServletRequest req,
                            @RequestParam(required = false) Integer pageNo) {

        if (board.getBoardId() == 0 || pageNo == null) {
            return "redirect:/board/list";
        }
        // 첨부파일 있을경우
        if (attach != null && attach.getSize() > 0) {
            BoardAttachDto attachment = new BoardAttachDto();
            ArrayList<BoardAttachDto> attachments = new ArrayList<>();
            try {
                String dir = req.getServletContext().getRealPath("/board-attachments");
                String userFileName = attach.getOriginalFilename();
                String savedFileName = Util.makeUniqueFileName(userFileName);
                attach.transferTo(new File(dir, savedFileName)); // 파일 저장

                attachment.setBoardId(board.getBoardId());
                attachment.setBoardAttachOriginName(userFileName);
                attachment.setBoardAttachSaveName(savedFileName);
                attachments.add(attachment);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            board.setAttachments(attachments);
        }

        try {
            boardService.modifyBoard(board);
        } catch (Exception ex) {
            ex.printStackTrace();
            return String.format("redirect:/board/edit?boardId=%d&pageNo=%d", board.getBoardId(), pageNo);
        }

        return String.format("redirect:/board/detail?boardId=%d&pageNo=%d", board.getBoardId(), pageNo);
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

    @GetMapping(path = { "/show-img/{attachNo}"})
    public String showImage(@PathVariable("attachNo") Integer attachNo, Model model)  {
        BoardAttachDto boardAttach = boardService.findBoardAttachByAttachNo(attachNo);
        model.addAttribute("attach", boardAttach);

        return "/userView/board/show-img";
    }
    @GetMapping(path = {"/download-img/{fileName}"})
    public ResponseEntity<Resource> showImageFile(@PathVariable("fileName")String fileName) {
        try {
            Path file = Paths.get(uploadPath).resolve(fileName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok().body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/updateBoardActive")
    @ResponseBody
    public String updateBoardActive(@RequestParam int boardId, @RequestParam boolean boardActive) {
        try {
            boardService.updateBoardActive(boardId, boardActive);
            return "success";
        } catch (Exception e) {
            return "failure";
        }
    }


}
