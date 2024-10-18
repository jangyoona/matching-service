package com.boyug.repository;

import com.boyug.dto.SessionDto;
import com.boyug.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionEntity, Integer> {
    Optional<SessionEntity> findBySessionNameAndSessionActiveTrue(String s);

    List<SessionEntity> findBySessionActiveTrue();
}
