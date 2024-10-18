package com.boyug.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TBL_GPTRECOMMENDCATEGORY")
public class GptRecommendCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int recommandCategory;

    @Column(nullable = false, length = 30)
    private String recommandName;

}
