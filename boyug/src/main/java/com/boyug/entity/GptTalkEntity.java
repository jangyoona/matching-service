package com.boyug.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="TBL_GPTTALK")
public class GptTalkEntity {

    @EmbeddedId
    private GptTalkId gptTalkId;

    @Builder.Default
    @Column
    private boolean gptTalkActive = true;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private UserEntity user;

    @OneToMany(mappedBy = "gptTalk", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<GptTalkDetailEntity> gptTalkDetails;
}