package com.boyug.controller;

import com.boyug.dto.UserDto;
import com.boyug.service.AccountService;
import com.boyug.service.SangDamService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = {"/userView"})
public class SangDamController {

    @Setter(onMethod_ = {@Autowired})
    SangDamService sangDamService;

    @Setter(onMethod_ = {@Autowired})
    AccountService accountService;

    @GetMapping("/advice")
    public String adviceForm() {
        return "userView/advice";
    }

    @GetMapping("/information")
    public String informationShow() {
        return "userView/information";
    }

    @GetMapping("/advice/call")
    @ResponseBody
    public String adviceCall(int userId, String content) {
        // 요청한 유저정보
        UserDto user = accountService.getUserInfo(userId);
        sangDamService.requestAdviceCall(user, content);
        return "success";
    }

}
