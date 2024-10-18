package com.boyug.controller;

import com.boyug.dto.BoardDto;
import com.boyug.dto.UserDto;
import com.boyug.ui.ProjectPager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping(path = {"/chat"})
public class ChatGptController {


    @GetMapping(path = {"/gpt"})
    public String chatgpt(HttpSession session) {
        return "/userView/gpt";
    }

//    @GetMapping(path = {"/list"})
//    public String listRange(@RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
//                            HttpServletRequest req, Model model) {
//
//        int pageSize = 5;        // 한 페이지에 표시하는 데이터 갯수
//        int pagerSize = 5;        // 한 번에 표시되는 페이지 번호 갯수
//        int dataCount = boardService.getBoardCounter();    // 전체 데이터 갯수
//        String uri = req.getRequestURI();
//        String linkUrl = uri.substring(uri.lastIndexOf("/") + 1); // 페이지 번호를 클릭했을 때 요청을 보낼 URL ( 목록 페이지 경로 )
//        String queryString = req.getQueryString();
//
//        int start = pageSize * (pageNo - 1) + 1; // 현재 페이지의 첫번째 데이터 행 번호
//
//        ProjectPager pager = new ProjectPager(dataCount, pageNo, pageSize, pagerSize, linkUrl, queryString);
//
//        List<BoardDto> boards = boardService.findBaordByRange2(pageNo - 1, pageSize);
//
//        model.addAttribute("boardList", boards); // Model 타입 전달인자에 데이터 저장 -> View(JSP)로 전달
//        model.addAttribute("pager", pager);
//        model.addAttribute("pageNo", pageNo);
//
//        return "/userView/board/list";    //  board/list
//    }

}


