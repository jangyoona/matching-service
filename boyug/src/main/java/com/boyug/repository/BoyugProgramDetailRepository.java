package com.boyug.repository;

import com.boyug.entity.BoyugProgramDetailEntity;
import com.boyug.entity.BoyugProgramEntity;
import com.boyug.entity.BoyugToUserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BoyugProgramDetailRepository extends JpaRepository<BoyugProgramDetailEntity, Integer> {

    @Query("SELECT COUNT(utb) > 0 FROM UserToBoyugEntity utb WHERE utb.user.id = :userId AND utb.boyugProgramDetail.id = :detailId")
    boolean existsByUserIdAndDetailId(@Param("userId") int userId,@Param("detailId") int detailId);

//    List<BoyugProgramDetailEntity> findByBoyugProgram_BoyugProgramIdIn(List<Integer> programIds);

    List<BoyugProgramDetailEntity> findByBoyugProgram_BoyugProgramId(int boyugProgramId);

    BoyugProgramDetailEntity findByBoyugProgramDetailId(int boyugProgramDetailId);

    Page<BoyugProgramDetailEntity> findByBoyugToUsers_UserSessionUserSessionIdAndBoyugToUsers_RequestIsOk(int userId, Pageable pageable, String 요청);

    List<BoyugProgramDetailEntity> findByBoyugProgram_BoyugProgramIdInAndBoyugProgramDetailDateAfter(List<Integer> programIds, LocalDate currentDate);

    @Query("SELECT d.boyugProgramDetailDate, COUNT(d) FROM BoyugProgramDetailEntity d GROUP BY d.boyugProgramDetailDate")
    List<Object[]> getDetailsByDate();

    Page<BoyugProgramDetailEntity> findByUserToBoyugs_BoyugProgramDetailIdInAndBoyugToUsers_RequestIsOk(List<Integer> btuIds, Pageable pageable, String 신청);
}
