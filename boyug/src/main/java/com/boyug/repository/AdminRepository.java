package com.boyug.repository;

import com.boyug.entity.BoyugUserEntity;
import com.boyug.entity.UserDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminRepository extends JpaRepository<BoyugUserEntity, Integer> {

//    @Query("SELECT bu, u " +
//            "FROM BoyugUserEntity bu " +
//            "JOIN bu.user u")
//    List<BoyugUserEntity> selectBU();
}