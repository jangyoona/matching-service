package com.boyug.dto;

import com.boyug.entity.BookmarkEntity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkDto {

    private UserDto fromUserId;
    private UserDto toUserId;

    public BookmarkEntity toEntity() {
        return BookmarkEntity.builder()
                .fromUser(fromUserId.toEntity())
                .toUser(toUserId.toEntity())
                .build();
    }

    public static BookmarkDto of(BookmarkEntity entity) {
        return BookmarkDto.builder()
                .fromUserId(UserDto.of(entity.getFromUser()))
                .toUserId(UserDto.of(entity.getToUser()))
                .build();
    }
}
