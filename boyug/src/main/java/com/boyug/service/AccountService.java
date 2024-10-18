package com.boyug.service;

import com.boyug.dto.BoyugUserDto;
import com.boyug.dto.BoyugUserFileDto;
import com.boyug.dto.ProfileImageDto;
import com.boyug.dto.UserDto;

import java.util.List;

public interface AccountService {

    UserDto getUserInfo(int userId);

    long dupCheckUserPhone(String userPhone);

    int getSocialIdByUserPhone(String userPhone);

    UserDto registerBoyugUser(UserDto user, BoyugUserDto boyugUser);

    void insertAttachments(List<BoyugUserFileDto> userFile ,ProfileImageDto profileImage);

    boolean updateUserPasswd(String userPhone, String userPw);

    List<ProfileImageDto> getUserProfileImage(UserDto user);

    BoyugUserDto getBoyugUserInfo(int userId);
}
