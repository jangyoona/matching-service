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
public class BoardDto {

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

    // 첨부파일 관련 필드
    private List<BoardAttachDto> attachments;

    // DTO -> Entity 변환
    public BoardEntity toEntity() {
        // UserEntity 객체를 생성
        UserEntity userEntity = UserEntity.builder()
                .userId(userId)
                .build();

        return BoardEntity.builder()
                .boardTitle(boardTitle)
                .writer(writer)
                .boardContent(boardContent)
                .boardRegdate(boardRegdate != null ? boardRegdate : new Date()) // 날짜가 없으면 현재 날짜
                .boardModifydate(boardModifydate != null ? boardModifydate : new Date()) // 날짜가 없으면 현재 날짜
                .boardCount(boardCount)
                .boardAnswer(boardAnswer != null ? boardAnswer : false) // null 값 처리
                .boardCategory(boardCategory)
                .boardActive(boardActive != null ? boardActive : true) // null 값 처리
                .user(userEntity) // userEntity 설정
                .attachments(attachments != null ? attachments.stream()
                        .map(BoardAttachDto::toEntity)
                        .collect(Collectors.toList()) : null) // 첨부파일 변환 로직 추가
                .build();
    }

    // Entity -> DTO 변환
    public static BoardDto of(BoardEntity entity) {
        return BoardDto.builder()
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
                .attachments(entity.getAttachments() != null ? entity.getAttachments().stream()
                        .map(BoardAttachDto::of)
                        .collect(Collectors.toList()) : null) // 첨부파일 변환 로직 추가
                .build();
    }
}
