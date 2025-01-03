package com.boyug.repository;

import com.boyug.dto.NotificationDto;
import com.boyug.entity.NotificationEntity;
import com.boyug.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {

//    @Query("SELECT n FROM NotificationEntity n WHERE n.toUser = :toUser AND n.isRead = :isRead ORDER BY n.sendDate DESC") // N+1 문제
//    @EntityGraph(attributePaths = {"chatRoom"}) // N+1 문제 + 밑도 끝도 없는 연관 엔티티 로딩으로 엄청나게 느림
//    @BatchSize(size = 5)
//    @Query("SELECT n " +
//            "FROM NotificationEntity n " +
//            "WHERE n.toUser = :toUser " +
//                "AND n.isRead = :isRead " +
//            "ORDER BY n.sendDate DESC")
    @Query("SELECT new com.boyug.dto.NotificationDto(n.notificationId, n.message, n.sendDate, c.chatRoomId) " + // Dto 프로젝션 => N+1 완벽 개선 및 속도 GooD
            "FROM NotificationEntity n " +
            "JOIN n.chatRoom c " +
            "WHERE n.toUser = :toUser " +
                "AND n.isRead = :isRead " +
            "ORDER BY n.sendDate DESC")
    List<NotificationDto> findNotificationsWithLimit(UserEntity toUser, boolean isRead, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationEntity n " +
            "SET n.isRead = true " +
            "WHERE n.notificationId = :notificationId " +
            "AND n.isRead = false")
    void updateIsReadByNotificationId(int notificationId);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationEntity n " +
            "SET n.isRead = true " +
            "WHERE n.toUser.userId = :userId " +
            "AND n.isRead = false")
    void updateIsReadByToUserId(int userId);
}
