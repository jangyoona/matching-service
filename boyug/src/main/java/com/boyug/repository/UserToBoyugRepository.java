package com.boyug.repository;

import com.boyug.entity.BoyugProgramDetailEntity;
import com.boyug.entity.UserSessionEntity;
import com.boyug.entity.UserToBoyugEntity;
import com.boyug.entity.UserToBoyugId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserToBoyugRepository extends JpaRepository<UserToBoyugEntity, UserToBoyugId> {
    List<UserToBoyugEntity> findByUser_UserId(int userId);

    UserToBoyugEntity findByBoyugProgramDetail_BoyugProgramDetailIdAndRequestIsOk(int boyugProgramDetailId, String 신청);

    UserToBoyugEntity findByBoyugProgramDetailIdAndUserId(int detailId, int userId);

    List<UserToBoyugEntity> findByUser_UserIdAndRequestIsOk(int userId, String 수락);
}
