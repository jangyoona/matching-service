package com.boyug.repository;

import com.boyug.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<UserEntity, Integer> {

    @Query("SELECT s.sessionName, COUNT(u) FROM UserEntity u " +
            "JOIN u.favorites s " +
            "GROUP BY s.sessionName")
    List<Object[]> countUsersBySession();

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.roles WHERE u.userId = :userId")
    Optional<UserEntity> findByIdWithRoles(@Param("userId") Integer userId);
}