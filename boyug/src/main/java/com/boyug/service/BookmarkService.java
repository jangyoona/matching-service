package com.boyug.service;

import com.boyug.dto.BookmarkDto;
import com.boyug.dto.UserDto;

import java.util.List;

public interface BookmarkService {

    void insertProgramBookmark(UserDto toUser, UserDto fromUser);

    boolean isProgramBoardBookmarked(int loginUserId, int OtherUserId);

    void deleteProgramBookmark(int loginUserId, int OtherUserId);

    List<BookmarkDto> findUserBookmarks(int userId);
}
