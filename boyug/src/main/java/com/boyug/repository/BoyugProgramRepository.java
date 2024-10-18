package com.boyug.repository;

import com.boyug.dto.BoyugProgramDto;
import com.boyug.entity.BoyugProgramDetailEntity;
import com.boyug.entity.BoyugProgramEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BoyugProgramRepository extends JpaRepository<BoyugProgramEntity, Integer> {

    List<BoyugProgramEntity> findByBoyugProgramActiveTrueAndBoyugProgramIsOpenTrue();

    List<BoyugProgramEntity> findTop5ByBoyugProgramActiveTrueAndBoyugProgramIsOpenTrue();

    List<BoyugProgramEntity> findTop5ByBoyugProgramActiveTrueAndBoyugProgramIsOpenTrueOrderByBoyugProgramIdDesc();

    List<BoyugProgramEntity> findByBoyugUser_UserId(int loginUserId);

    List<BoyugProgramEntity> findByBoyugProgramActiveTrueAndBoyugProgramIsOpenTrueOrderByBoyugProgramIdDesc();

    // 날짜별 프로그램 수를 조회하는 쿼리
    @Query("SELECT p.boyugProgramRegdate, COUNT(p) FROM BoyugProgramEntity p GROUP BY p.boyugProgramRegdate")
    List<Object[]> getProgramsByDate();

    Page<BoyugProgramEntity> findByBoyugUser_UserId(int userId, Pageable pageable);

    Page<BoyugProgramEntity> findByBoyugProgramActiveTrueAndBoyugProgramIsOpenTrueOrderByBoyugProgramRegdateDesc(Pageable pageable);

    @Query(value = "SELECT " +
            "    TB.*, " +
            "    DISTNACE_WGS84(:lat, :lng, TU.USERLATITUDE, TU.USERLONGITUDE) AS DISTANCE " +
            "FROM TBL_USER TU, TBL_BOYUGPROGRAM TB " +
            "WHERE TU.USERID = TB.USERID " +
            "ORDER BY DISTANCE ASC", nativeQuery = true)
    List<BoyugProgramEntity> findBoyugProgramOrderByDistanceAsc(double lat, double lng);
}
