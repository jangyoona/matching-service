package com.boyug.repository;

import com.boyug.entity.BoyugToUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoyugToUserRepository extends JpaRepository<BoyugToUserEntity, Integer> {

    List<BoyugToUserEntity> findByUserSession_UserSessionId(int userSessionId);

    List<BoyugToUserEntity> findByUserSession_UserSessionIdAndRequestIsOk(int userSessionId, String 요청);

    BoyugToUserEntity findByUserSession_UserSessionIdAndBoyugProgramDetail_BoyugProgramDetailId(int userId, int programDetailId);

    boolean existsByUserSession_UserSessionIdAndBoyugProgramDetail_BoyugProgramDetailId(int userId, int boyugProgramDetailId);

    BoyugToUserEntity findByBoyugProgramDetail_BoyugProgramDetailId(int boyugProgramDetailId);

    BoyugToUserEntity findByBoyugProgramDetail_BoyugProgramDetailIdAndRequestIsOk(int boyugProgramDetailId, String 수락);
}
