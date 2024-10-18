package com.boyug.repository;


import com.boyug.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Integer> {

    // 해당 RoomNumber 채팅메세지 가져오기
    List<ChatMessageEntity> findByChatRoomChatRoomIdOrderByChatSendTimeAsc(int chatRoomId);

    // 메세지 읽음 처리
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessageEntity c SET c.toIsRead = true WHERE c.chatRoom.chatRoomId = :chatRoomId AND c.toUser.userId = :toUserId")
    void updateToIsRead(int chatRoomId, int toUserId);

//    @Query("SELECT COUNT(DISTINCT cr.chatRoomId) " +
    @Query("SELECT DISTINCT cr.chatRoomId " +
                "FROM ChatMessageEntity cm " +
                "JOIN cm.chatRoom cr " +
                "WHERE ((cm.toUser.userId = :myUserId AND cm.fromUser.userId = :otherUserId) " +
                "OR (cm.toUser.userId = :otherUserId AND cm.fromUser.userId = :myUserId)) " +
                "AND cr.boyugChatActive = true")
    Integer findChatRoomByUserIdsAndBoyugType(int myUserId, int otherUserId);

//    @Query("SELECT COUNT(DISTINCT cr.chatRoomId) " +
    @Query("SELECT DISTINCT cr.chatRoomId " +
                "FROM ChatMessageEntity cm " +
                "JOIN cm.chatRoom cr " +
                "WHERE ((cm.toUser.userId = :myUserId AND cm.fromUser.userId = :otherUserId) " +
                "OR (cm.toUser.userId = :otherUserId AND cm.fromUser.userId = :myUserId)) " +
                "AND cr.userChatActive = true")
    Integer findChatRoomByUserIdsAndUserType(int myUserId, int otherUserId);
}
