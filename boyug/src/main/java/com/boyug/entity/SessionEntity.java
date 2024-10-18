package com.boyug.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TBL_SESSION")
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int sessionId;

    @Column(nullable = false, length = 30)
    private String sessionName;

    @Builder.Default
    @Column
    private Boolean sessionActive = true;

    @ManyToMany(mappedBy = "favorites")
    private Set<UserEntity> users;

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BoyugProgramDetailEntity> boyugProgramDetails;

}
