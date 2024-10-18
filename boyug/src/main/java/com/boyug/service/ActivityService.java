package com.boyug.service;

import com.boyug.dto.*;
import com.boyug.entity.SessionEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ActivityService {

    List<SessionDto> findAllSessions();

    void writeBoyugProgram(BoyugProgramDto program);

    Page<BoyugProgramDto> findAllList(int page, int size);

    BoyugProgramDto findBoyugProgram(Integer boyugProgramId);

    void insertUserToBoyug(int userId, int detailNo);

    boolean isUserApplied(int userId, int detailId);

    Boolean deleteBoyugProgram(int boyugProgramId, int userId);

    void editBoyugProgram(BoyugProgramDto program);

    List<BoyugProgramDto> find5Program(int userId);

    List<SessionDto> findUserFavorites(int userId);

    void insertNoinRegister(int userId);

    boolean findRegisterUser(int userId);

    void updateNoinUnRegister(int userId);

    ProfileImageDto findProfileImage(int userId);

    Page<UserDto> findNoinRegisterList(int page, int size);

    UserDto findNoinRegisterUser(int userId);

    List<BoyugProgramDetailDto> findBoyugProgramDetails(int loginUserId, int userId);

    void insertBoyugToUser(List<Integer> checkedValue, int userId);

    UserDto findUserByUserSessionId(int sessionId);

    List<BoyugProgramDto> find5ProgramChltlstns(int userId);

    List<BoyugProgramDto> find5ProgramReRoll(int userId);
}
