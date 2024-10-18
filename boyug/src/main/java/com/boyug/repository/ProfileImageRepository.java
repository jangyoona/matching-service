package com.boyug.repository;

import com.boyug.dto.UserDto;
import com.boyug.entity.ProfileImageEntity;
import com.boyug.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileImageRepository extends JpaRepository<ProfileImageEntity, Integer> {

    List<ProfileImageEntity> findProfileImageByUserUserId(int userId);

}
