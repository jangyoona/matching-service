package com.boyug.dto;

import com.boyug.entity.BoardEntity;
import com.boyug.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardoldDto {

    private int boardId;
    private String boardTitle;
    private String boardContent;
    private String writer;

    private Date boardRegdate;
    private Date boardModifydate;
    private int boardCount;
    private Boolean boardAnswer;
    private int boardCategory;
    private Boolean boardActive;

    // 회원번호
    private int userId;

    // board 테이블과 boardattach 테이블 사이의 1 : Many 관계를 구현하는 필드
    private List<BoardAttachDto> attachments;

    public BoardEntity toEntity() {
        // UserEntity 객체를 생성
        UserEntity userEntity = UserEntity.builder()
                .userId(userId)
                .build();

        return  BoardEntity.builder()
                .boardTitle(boardTitle)
                .writer(writer)
                .boardContent(boardContent)
//                .boardRegdate(boardRegdate)
//                .boardModifydate(boardModifydate)
//                .boardCount(boardCount)
//                .boardAnswer(boardAnswer)
//                .boardCategory(boardCategory)
//                .boardActive(boardActive)
                .user(userEntity) // userEntity 설정
                .attachments(attachments.stream().map(BoardAttachDto::toEntity).collect(Collectors.toList())) // 변환 로직 추가
                .build();

    }
    public static BoardoldDto of(BoardEntity entity) {
        return BoardoldDto.builder()
                .boardId(entity.getBoardId())
                .boardTitle(entity.getBoardTitle())
                .writer(entity.getWriter())
                .boardContent(entity.getBoardContent())
                .boardRegdate(entity.getBoardRegdate())
                .boardModifydate(entity.getBoardModifydate())
                .boardCount(entity.getBoardCount())
                .boardAnswer(entity.getBoardAnswer())
                .boardCategory(entity.getBoardCategory())
                .boardActive(entity.getBoardActive())
                .build();
    }
}
