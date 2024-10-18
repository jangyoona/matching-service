package com.boyug.repository;

import com.boyug.dto.BoardDto;
import com.boyug.entity.BoardAttachEntity;
import com.boyug.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Integer> {

    // 특정 userId의 게시글 수를 반환하는 메서드
    int countByUser_UserId(int userId);
    // 특정 userId의 게시글을 페이징하여 가져오는 메서드
    Page<BoardEntity> findByUser_UserId(int userId, Pageable pageable);


    @Query("SELECT b FROM BoardEntity b WHERE b.boardCategory = :boardCategory")
    List<BoardEntity> findByCategory(@Param("boardCategory") int boardCategory);

    @Query(value = "SELECT ba FROM BoardAttachEntity ba WHERE ba.boardAttachId = :boardAttachId")
    BoardAttachEntity findBoardAttachByAttachNo(@Param("boardAttachId") int boardAttachId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM BoardAttachEntity ba WHERE ba.boardAttachId = :boardAttachId")
    void deleteBoardAttachByAttachNo(@Param("boardAttachId")int boardAttachId);

    @Modifying
    @Transactional
    @Query("UPDATE BoardEntity b SET b.boardTitle = :boardTitle, b.boardContent = :boardContent, b.boardAnswer = true WHERE b.boardId = :boardId")

    void updateBoardByBoardId(@Param("boardId") int boardId, @Param("boardTitle") String boardTitle, @Param("boardContent") String boardContent);
}
