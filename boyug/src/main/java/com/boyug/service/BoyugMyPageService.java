package com.boyug.service;

import com.boyug.dto.*;

import java.util.List;

public interface BoyugMyPageService {
    List<BoyugProgramDto> findBoyugPrograms(int userId);

    List<UserToBoyugDto> findUserToBoyugs(int userId);

    List<UserDto> findPersonalUsers(List<UserToBoyugDto> boyugToUsers);

//    List<UserDto> findPersonalUsers(int userId);

    List<BoyugProgramDetailDto> findBoyugProgramDetails(List<UserToBoyugDto> boyugToUsers);

    List<BoyugToUserDto> findBoyugToUsers(int userId);

    List<BoyugProgramDetailDto> findBoyugProgramDetailsWithBtu(List<BoyugToUserDto> boyugToUsers);

    List<UserDto> findPersonalUsersWithBtu(List<BoyugToUserDto> boyugToUsers);

    void modifyUserToBoyug(int detailId, int userId);

    void modifyUserToBoyugRefuse(int detailId, int userId);

    List<UserToBoyugDto> findMatchingUserToBoyugs(int userId);

    List<BoyugProgramDetailDto> findPagingUserToBoyugs(int pageNo, int count, int userId);

    List<BoyugProgramDto> findPagingBoyugPrograms(int pageNo, int count, int userId);

    List<ProfileImageDto> findProfileImages(int userId);

    void removeProfileImage(int imageId);

    BoyugUserDto findBoyugUser(int userId);

    void modifyBoyugUser(UserDto user, BoyugUserDto boyugUser);

    List<BoyugProgramDto> findBoyugProgramDetails2(int userId);

    List<UserDto> findRequestUsers(List<BoyugProgramDto> programs);

    List<BoyugProgramDto> findMatchingBoyugToUsers(int userId);

//    List<BoyugToUserDto> findMatchingBoyugToUsers2(List<BoyugProgramDto> programs);

    List<BoyugToUserDto> findConfirmBoyugToUsers(int userId);
}
