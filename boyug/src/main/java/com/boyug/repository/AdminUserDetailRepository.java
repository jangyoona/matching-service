package com.boyug.repository;

import com.boyug.entity.BoyugUserEntity;
import com.boyug.entity.UserDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserDetailRepository extends JpaRepository<UserDetailEntity, Integer> {

}