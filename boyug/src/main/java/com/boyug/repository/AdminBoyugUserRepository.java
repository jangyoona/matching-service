package com.boyug.repository;

import com.boyug.entity.BoyugUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface AdminBoyugUserRepository extends JpaRepository<BoyugUserEntity, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE BoyugUserEntity b SET b.boyugUserConfirm = true WHERE b.user.userId = :userId")
    int updateConfirm(int userId);
}