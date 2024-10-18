package com.boyug.dto;

import com.boyug.entity.BoardAttachEntity;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardAttachDto {

    private int boardAttachId;
    private String boardAttachOriginName;
    private String boardAttachSaveName;
    private Date boardAttachSavedDate;

    // 게시판 번호
    private int boardId;

    public BoardAttachEntity toEntity() {
        return BoardAttachEntity.builder()
                .boardAttachOriginName(boardAttachOriginName)
                .boardAttachSaveName(boardAttachSaveName)
                .boardAttachSavedDate(boardAttachSavedDate)
                .build();
    }
    public static BoardAttachDto of(BoardAttachEntity entity) {
        return BoardAttachDto.builder()
                .boardAttachId(entity.getBoardAttachId())
                .boardAttachOriginName(entity.getBoardAttachOriginName())
                .boardAttachSaveName(entity.getBoardAttachSaveName())
                .boardAttachSavedDate(entity.getBoardAttachSavedDate())
                .build();
    }

}
