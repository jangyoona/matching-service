package com.boyug.service;

import com.boyug.dto.*;
import com.boyug.entity.SessionEntity;
import com.boyug.entity.UserDetailEntity;
import com.boyug.entity.UserEntity;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface AdminService {

    List<UserDetailDto> findAllUser();
    List<BoyugUserDto> findAllBoyug();

    void boyugcomfirm(int userId);

    void updateHealthStatus(int userId, String userHealth);

    void updateProtectorPhone(int userId, String protectorPhone);

    UserDetailEntity findById(int userId);

    void save(UserDetailEntity user);

    boolean updateUserName(int userId, String userName);

    int getBoyugUserCount();

    int getUserDetailCount();

    int getBoyugProgramDetailCount();

    int getBoyugProgramCount();

    UserEntity findByUserId(int userId);

    void saveuser(UserEntity user);

    void updateBoyugChildNum(int userId, int boyugChildNum);

    void updateBoyugEmail(int userId, String boyugEmail);

    void updateBoyugName(int userId, String boyugName);

    void updateUserBirth(int userId, Date birthDate);

    BoyugUserDto getBoyugUserById(int userId);

    List<BoyugProgramDto> getBoyugProgramsByUserId(int userId);

    List<BoyugProgramDto> getAllBoyugPrograms();

    List<Object[]> getUserCountBySession();

    List<Object[]> getStatisticsByDate();

    List<Object[]> getDetailsByDate();

    UserDetailDto findUserDetailById(int userId);

    List<UserToBoyugDto> findUserToBoyugByUserId(Integer userId);

    UserDto findUserWithRolesById(Integer userId);


    void addRoleToUser(Integer userId, String roleAdmin);

    void removeRoleFromUser(Integer userId, String roleAdmin);

    List<SessionDto> findAllSession();

    void addSession(SessionEntity entity);

    void deleteSession(int sessionId);

    void updateSessionActive(int sessionId, boolean isActive);

    void updateSessionName(int sessionId, String newSessionName);

    List<SangdamDto> findAllSangdam();

    void updateSangdamActive(int sangdamId, String sangdamActive);

    void updateProgramStatus(int programId, boolean activeStatus);

    void updateProgramDetailStatus(int detailId, boolean activeStatus);

    List<UserSessionDto> findAllUserSession();

    void updateUserSessionActive(int userSessionId, boolean isActive);
}
