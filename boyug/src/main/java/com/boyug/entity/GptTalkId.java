package com.boyug.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class GptTalkId implements Serializable {

    @Column(name = "gptTalkId")
    private int gptTalkId;
    @Column(name = "userId")
    private int userId;

    public GptTalkId() {}
    public GptTalkId(int gptTalkId, int userId) {
        this.gptTalkId = gptTalkId;
        this.userId = userId;
    }
}