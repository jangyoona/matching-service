package com.boyug.repository;


import com.boyug.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Integer> {

    @Query(value = "SELECT TBL_CHATROOM_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
    int findNextChatRoomId();

    @Query("SELECT cr FROM ChatRoomEntity cr JOIN cr.chatMessages c WHERE cr.userChatActive = true AND c.fromUser.userId = :userId OR c.toUser.userId = :userId ORDER BY c.chatSendTime DESC")
    List<ChatRoomEntity> findAllChatRoomByUserId(int userId);

    @Query("SELECT cr FROM ChatRoomEntity cr JOIN cr.chatMessages c WHERE cr.boyugChatActive = true AND c.fromUser.userId = :boyugUserId OR c.toUser.userId = :boyugUserId ORDER BY c.chatSendTime DESC")
    List<ChatRoomEntity> findAllChatRoomByBoyugUserId(int boyugUserId);

}
