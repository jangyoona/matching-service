package com.boyug.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "TBL_BOOKMARK")
public class BookmarkEntity {
    // 복합키 클래스
    @EmbeddedId
    private BookmarkId bookmarkId;

    @MapsId("fromUserId") // 식별자 클래스의 ID와 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fromUserId")
    private UserEntity fromUser;

    @MapsId("toUserId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toUserId")
    private UserEntity toUser;
}
