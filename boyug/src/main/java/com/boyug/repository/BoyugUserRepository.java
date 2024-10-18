package com.boyug.repository;

import com.boyug.entity.BoyugUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoyugUserRepository extends JpaRepository<BoyugUserEntity, Integer> {
    BoyugUserEntity findByUserId(int userId);
}
