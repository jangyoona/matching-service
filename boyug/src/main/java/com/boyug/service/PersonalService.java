package com.boyug.service;

import com.boyug.dto.*;
import com.boyug.entity.BoyugToUserEntity;

import java.util.List;

public interface PersonalService {
    int addPersonalUser(UserDto userDto, UserDetailDto userDetailDto);

    void addProfileImage(ProfileImageDto userFile);

    String findUserPhone(String userPhone);

    UserDetailDto findUserDetail(int userId);

    UserDto findUser(int userId);

    List<SessionDto> findAllSession();

    SessionDto findSessionId(String s);

    void modfiyUserAndUserDetail(UserDto user, UserDetailDto userDetail);

    List<UserDto> findBoyugUsers();

    List<BoyugUserDto> findBoyugUsersDetail();

    List<BoyugProgramDetailDto> findBoyugProgramDetail(int userId);

    BoyugProgramDto findBoyugProgram(int boyugProgramId);

    List<BoyugProgramDetailDto> findPagingBoyugProgramDetails(int pageNo, int count);

    List<BoyugProgramDetailDto> findBoyugProgramDetailAll(int userId);

    List<BoyugProgramDetailDto> findPagingBoyugProgramDetailsInBTU(int pageNo, int count, int userId);


    void modifyBoyugToUser(int userId, int programDetailId);

    List<BoyugProgramDetailDto> findConfirmBoyugProgramDetailAll(int userId);

    void modifyBoyugToUserRefuse(int userId, int programDetailId);

    List<BoyugProgramDetailDto> findPagingBoyugProgramDetailsInMatching(int pageNo, int count, int userId);
}
