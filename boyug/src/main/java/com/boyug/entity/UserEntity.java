package com.boyug.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TBL_USER")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int userId;

    @Column(unique = true)
    private String userPhone;

    @Column
    private String userPw;

    @Column
    private String userName;

    @Column
    private String userAddr1;

    @Column
    private String userAddr2;

    @Builder.Default
    @Column
    private String userAddr3 = "없음";

    @Builder.Default
    @Column
    private Date userRegDate  = new Date();

    @Builder.Default
    @Column
    private Boolean userActive = true;

    @Column
    private String userLatitude;

    @Column
    private String userLongitude;

    // 소셜 로그인 구분 일반 로그인은 null로 냅두면 됨
    @Column
    @Builder.Default
    private String socialId = null;

    @Column
    private int userCategory;

    @Column
    private String userType; // ROLE_ADMIN OR ROLE_BOYUG, ROLE_USER

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name="TBL_USERROLE",
            joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "roleId")
    )
    private Set<RoleEntity> roles;

    @ManyToMany
    @JoinTable(
            name="TBL_USERFAVORITE",
            joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "sessionId")
    )
    private Set<SessionEntity> favorites;

    // user 테이블과 profileimage 테이블 사이의 1 : Many 관계를 구현하는 필드
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProfileImageEntity> images;

//    @MapsId
//    @OneToOne
//    @JoinColumn(name="userId")
//    private UserSessionEntity userSession;

}
