package com.boyug.repository;

import com.boyug.entity.UserSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, Integer> {
    Optional<UserSessionEntity> findByUserSessionIdAndUserSessionActive(int userId, boolean b);

    Page<UserSessionEntity> findByUserSessionActiveIsTrue(Pageable pageable);
}
