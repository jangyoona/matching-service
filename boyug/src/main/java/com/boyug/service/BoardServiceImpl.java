package com.boyug.service;

import com.boyug.dto.BoardAttachDto;
import com.boyug.dto.BoardDto;
import com.boyug.entity.BoardAttachEntity;
import com.boyug.entity.BoardEntity;
import com.boyug.repository.BoardAttachRepository;
import com.boyug.repository.BoardRepository;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class BoardServiceImpl implements BoardService {

    @Setter
    private BoardRepository boardRepository;
    @Setter
    private BoardAttachRepository boardAttachRepository;

    @Setter
    private TransactionTemplate transactionTemplate;

    @Override
    public void writeBoard(BoardDto boardDto) {
        BoardEntity boardEntity = boardDto.toEntity();

        List<BoardAttachEntity> attachments = boardDto.getAttachments().stream().map((attach) ->{
            BoardAttachEntity attachEntity = attach.toEntity();
            attachEntity.setBoard(boardEntity);
            return attachEntity;
        }).toList();
        boardEntity.setAttachments(attachments);
        boardRepository.save(boardEntity);
    }

    @Override
    public List<BoardDto> findAllBoard() {
        List<BoardEntity> boardEntities = boardRepository.findAll();
        List<BoardDto> boards = new ArrayList<>();
        for(BoardEntity boardEntity : boardEntities){
            boards.add(BoardDto.of(boardEntity));
        }
        return boards;
    }
    @Override
    public List<BoardDto> findAllNotice() {
        List<BoardEntity> boardEntities = boardRepository.findByCategory(5);
        List<BoardDto> notices = new ArrayList<>();
        for(BoardEntity boardEntity : boardEntities){
            notices.add(BoardDto.of(boardEntity));
        }
        return notices;
    }


    @Override
    public int getBoardCounter() {
        return (int)boardRepository.count();
    }

//    @Override
//    public List<BoardDto> findBaordByRange(int start, int count) {
//
//        List<BoardDto> boards = boardMapper.selectBoardByRange(start, start + count);
//        return boards;
//
//    }

    @Override
    public List<BoardDto> findBoardByRange2(int pageNo, int count) {
        Pageable pageable = PageRequest.of(pageNo, count, Sort.by(Sort.Direction.DESC, "boardId"));
        Page<BoardEntity> page = boardRepository.findAll(pageable);
        List<BoardDto> boards = new ArrayList<>();
        for (BoardEntity boardEntity : page.getContent()) {
            boards.add(BoardDto.of(boardEntity));
        }
        return boards;
    }
    @Override
    public int getBoardCountByUserId(int userId) {
        return boardRepository.countByUser_UserId(userId);
    }
    @Override
    public List<BoardDto> findBoardByRangeUserId(int pageNo, int count, int userId) {
        Pageable pageable = PageRequest.of(pageNo, count, Sort.by(Sort.Direction.DESC, "boardId"));
        Page<BoardEntity> page = boardRepository.findByUser_UserId(userId, pageable);
        List<BoardDto> boards = new ArrayList<>();
        for (BoardEntity boardEntity : page.getContent()) {
            boards.add(BoardDto.of(boardEntity));
        }
        return boards;
    }

    @Override
    public BoardDto findBoardByBoardId(int boardId) {

        Optional<BoardEntity> entity = boardRepository.findById(boardId);
        if (entity.isPresent()) {
            BoardEntity boardEntity = entity.get();
            BoardDto board = BoardDto.of(boardEntity);

            List<BoardAttachDto> attachments =
                    boardEntity.getAttachments().stream()
                            .map(BoardAttachDto::of)
                            .toList();
            board.setAttachments(attachments);
            return board;
        } else {
            return null;
        }

    }

    @Override
    public BoardAttachDto findBoardAttachByAttachNo(int boardAttachId) {
        BoardAttachEntity entity = boardRepository.findBoardAttachByAttachNo(boardAttachId);
        return BoardAttachDto.of(entity);
    }

    @Override
    public void deleteBoard(int boardId) {
        BoardEntity entity = boardRepository.findById(boardId).get();
        entity.setBoardActive(false);
        boardRepository.save(entity);

    }

    @Override
    public void deleteBoardAttach(int boardAttachId) {
        boardRepository.deleteBoardAttachByAttachNo(boardAttachId); // 3
    }

    @Override
    public void modifyBoard(BoardDto board) {

        // 엔티티 조회
//        BoardEntity entity = boardRepository.findById(board.getBoardId()).get();
        BoardEntity entity = boardRepository.findById(board.getBoardId()).orElseThrow(() -> new RuntimeException("글을 찾을 수 없습니다."));
        // 제목과 내용 글수정시간 업데이트
        entity.setBoardTitle(board.getBoardTitle());
        entity.setBoardContent(board.getBoardContent());
        entity.setBoardModifydate(new Date());

        // 첨부파일이 있는 경우 처리
        if (board.getAttachments() != null) {
            List<BoardAttachEntity> attachEntities =
                    board.getAttachments().stream().map((attach) -> {
                        BoardAttachEntity attachEntity = attach.toEntity();
                        attachEntity.setBoard(entity);
                        return attachEntity;
                    }).toList();
            entity.getAttachments().addAll(attachEntities);
        }
        // 엔티티 저장
        boardRepository.save(entity);
    }

    @Override
    public void editinquiryboard(BoardDto boardDto) {
        boardRepository.updateBoardByBoardId(
                boardDto.getBoardId(),
                boardDto.getBoardTitle(),
                boardDto.getBoardContent()
        );
    }

    @Override
    public List<BoardDto> findBoardByNotice() {
        // 카테고리가 5인 게시물만 조회
        List<BoardEntity> boardEntities = boardRepository.findByCategory(5);
        List<BoardDto> notices = new ArrayList<>();

        // 조회된 엔티티를 DTO로 변환
        for (BoardEntity boardEntity : boardEntities) {
            notices.add(BoardDto.of(boardEntity));
        }

        return notices;
    }

    @Transactional
    public void updateBoardActive(int boardId, boolean boardActive) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시판 ID가 존재하지 않습니다: " + boardId));
        board.setBoardActive(boardActive);
        boardRepository.save(board);
    }


}
