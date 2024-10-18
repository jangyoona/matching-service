package com.boyug.repository;

import com.boyug.entity.UserDetailEntity;
import com.boyug.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalDetailRepository extends JpaRepository<UserDetailEntity, Integer> {


    UserDetailEntity findUserDetailByUserId(int userId);
}
