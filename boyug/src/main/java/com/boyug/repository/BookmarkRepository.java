package com.boyug.repository;

import com.boyug.entity.BookmarkEntity;
import com.boyug.entity.BookmarkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, BookmarkId> {


    @Query("SELECT b FROM BookmarkEntity b WHERE b.bookmarkId.fromUserId = :userId")
    List<BookmarkEntity> findBookmarkByFromUserId(int userId);
}
