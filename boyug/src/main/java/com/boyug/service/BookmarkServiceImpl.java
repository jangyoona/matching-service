package com.boyug.service;

import com.boyug.dto.BookmarkDto;
import com.boyug.dto.UserDto;
import com.boyug.entity.BookmarkEntity;
import com.boyug.entity.BookmarkId;
import com.boyug.repository.BookmarkRepository;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookmarkServiceImpl implements BookmarkService{

    @Setter
    BookmarkRepository bookmarkRepository;

    @Override
    public void insertProgramBookmark(UserDto fromUser, UserDto toUser) {
        BookmarkEntity bookmark = new BookmarkEntity();
        BookmarkId bookmarkId = new BookmarkId(fromUser.getUserId(), toUser.getUserId());
        bookmark.setBookmarkId(bookmarkId);
        bookmark.setFromUser(fromUser.toEntity());
        bookmark.setToUser(toUser.toEntity());
        bookmarkRepository.save(bookmark);
    }

    @Override
    public boolean isProgramBoardBookmarked(int loginUserId, int OtherUserId) {
        BookmarkId bookmarkId = new BookmarkId(loginUserId, OtherUserId);
        return bookmarkRepository.findById(bookmarkId).isPresent();
    }

    @Override
    public void deleteProgramBookmark(int loginUserId, int OtherUserId) {
        BookmarkId bookmarkId = new BookmarkId(loginUserId, OtherUserId);
        bookmarkRepository.deleteById(bookmarkId);
    }

    @Override
    public List<BookmarkDto> findUserBookmarks(int userId) {
        return bookmarkRepository.findBookmarkByFromUserId(userId).stream()
                .map(BookmarkDto::of)
                .collect(Collectors.toList());
    }

//    // 복합키를 구성
//    BookmarkId bookmarkId = new BookmarkId(fromUser.getId(), toUser.getId());
//
//    // 복합키로 조회
//    Optional<BookmarkEntity> bookmark = bookmarkRepository.findById(bookmarkId);
}
