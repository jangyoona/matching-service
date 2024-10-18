package com.boyug.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TBL_GPTRECOMMEND")
public class GptRecommendEntity {

    @Column(nullable = false, length = 2000)
    private String recommandDesc;
    @Builder.Default
    @Column
    private Date recommandRegdate = new Date();
    @Builder.Default
    @Column
    private boolean recommandActive = true;

    @Id
    @ManyToOne
    @JoinColumn(name = "sessionId")
    private SessionEntity session;

    @ManyToOne
    @JoinColumn(name = "recommandCategory")
    private GptRecommendCategoryEntity recommendCategory;

}
