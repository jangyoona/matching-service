package com.boyug.service;

import com.boyug.dto.BoardAttachDto;
import com.boyug.dto.BoardDto;

import java.util.List;

public interface BoardService {
    void writeBoard(BoardDto boardDto);

    List<BoardDto> findAllNotice();
    List<BoardDto> findAllBoard();

    int getBoardCounter();
//    List<BoardDto> findBaordByRange(int start, int count);
    List<BoardDto> findBoardByRange2(int pageNo, int count);
    int getBoardCountByUserId(int userId);
    List<BoardDto> findBoardByRangeUserId(int pageNo, int count, int userId);

    BoardDto findBoardByBoardId(int boardId);

    BoardAttachDto findBoardAttachByAttachNo(int boardAttachId);

    void deleteBoard(int boardId);

    void deleteBoardAttach(int boardAttachId);

    void modifyBoard(BoardDto board);

    void editinquiryboard(BoardDto boardDto);

    List<BoardDto> findBoardByNotice();

    void updateBoardActive(int boardId, boolean boardActive);
}
