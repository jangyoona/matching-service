package com.boyug.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class BookmarkId implements Serializable {

    // UserEntity의 ID 타입에 맞게 설정
    @Column(name = "fromUserId")
    private int fromUserId;
    @Column(name = "toUserId")
    private int toUserId;

    public BookmarkId() {}
    public BookmarkId(int fromUserId, int toUserId) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
    }
}