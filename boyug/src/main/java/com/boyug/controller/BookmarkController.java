package com.boyug.controller;

import com.boyug.dto.BookmarkDto;
import com.boyug.dto.UserDto;
import com.boyug.oauth2.CustomOAuth2User;
import com.boyug.security.WebUserDetails;
import com.boyug.service.AccountService;
import com.boyug.service.BookmarkService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class BookmarkController {

    @Setter(onMethod_ = { @Autowired})
    private BookmarkService bookmarkService;

    @Setter(onMethod_ = { @Autowired})
    private AccountService accountService;


    @GetMapping("bookmark-add/{userId}")
    @ResponseBody
    public String bookmarkAdd(@PathVariable int userId) {
        // (나)
        UserDto fromUser = getUserDetails().getUser();

        // (상대방)
        UserDto toUser = accountService.getUserInfo(userId);
        bookmarkService.insertProgramBookmark(fromUser, toUser);

        return "success";
    }

    @GetMapping("bookmark-delete/{userId}")
    @ResponseBody
    public String bookmarkDelete(@PathVariable int userId) {
        // toUser (나)
        UserDto toUser = getUserDetails().getUser();
        bookmarkService.deleteProgramBookmark(toUser.getUserId(), userId);

        return "success";
    }

    @GetMapping("bookmark-get")
    @ResponseBody
    public List<BookmarkDto> userBookmarkShow() { // 북마크 목록 조회할 때 쓰세요

        UserDto toUser = getUserDetails().getUser();
        List<BookmarkDto> bookmarks = bookmarkService.findUserBookmarks(toUser.getUserId());
        return bookmarks;
    }



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
