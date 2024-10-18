package com.boyug.repository;

import com.boyug.entity.NotificationEntity;
import com.boyug.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {

    // 받는 사람 기준
//    List<NotificationEntity> findByToUserAndIsReadOrderBySendDateDesc(UserEntity toUser, boolean isRead);

    @Query("SELECT n FROM NotificationEntity n WHERE n.toUser = :toUser AND n.isRead = :isRead ORDER BY n.sendDate DESC")
    List<NotificationEntity> findNotificationsWithLimit(UserEntity toUser, boolean isRead, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.notificationId = :notificationId")
    void updateIsReadByNotificationId(int notificationId);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.toUser.userId = :userId")
    void updateIsReadByToUserId(int userId);
}
