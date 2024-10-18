package com.boyug.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="TBL_GPTTALKDETAIL")
public class GptTalkDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int gptDetailId;

    @Column(nullable = false, length = 1000)
    private String gptRequest;

    @Column(nullable = false, length = 10000)
    private String gptResponse;

    @Builder.Default
    @Column
    private Date gptSendTime = new Date();

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "gptTalkId", referencedColumnName = "gptTalkId"),
            @JoinColumn(name = "userId", referencedColumnName = "userId")
    })
    private GptTalkEntity gptTalk;
}