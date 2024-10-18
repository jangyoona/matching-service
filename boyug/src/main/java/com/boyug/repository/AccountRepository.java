package com.boyug.repository;

import com.boyug.dto.ProfileImageDto;
import com.boyug.entity.RoleEntity;
import com.boyug.entity.SessionEntity;
import com.boyug.entity.UserDetailEntity;
import com.boyug.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface AccountRepository  extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByUserPhone(String userPhone);

    Optional<UserEntity> findBySocialId(String socialId);

    // 아이디 중복 체크
    long countByUserPhone(String userPhone);

    // 소셜 로그인 유저인지 체크
    int countByUserPhoneAndSocialIdIsNotNull(String userPhone);

    @Query(value = "SELECT re FROM RoleEntity re WHERE re.roleName = :roleName")
    RoleEntity findRoleByRoleName(String roleName);

    @Query("SELECT bu.boyugUserConfirm " +
            "FROM BoyugUserEntity bu " +
            "JOIN bu.user u " +
            "WHERE u.userPhone = :userPhone")
    boolean boyugUserIsConfirm(String userPhone);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO TBL_BOYUGUSERFILE(boyugUserFileId, userId, fileOriginName, fileSavedName, savedDate)" +
                    "VALUES(TBL_BOYUGUSERFILE_SEQ.NEXTVAL, :userId, :fileOriginName, :fileSavedName, :savedDate)", nativeQuery = true)
    void insertBoyugUserFile(@Param("userId") int userId,
                             @Param("fileOriginName") String fileOriginName,
                             @Param("fileSavedName") String fileSavedName,
                             @Param("savedDate") Date savedDate);

    @Query("SELECT s " +
            "FROM UserEntity u " +
            "JOIN u.favorites f " +
            "JOIN SessionEntity s ON f.sessionId = s.sessionId " +
            "WHERE u.userId = :userId")
    List<SessionEntity> findFavoritesByUserId(@Param("userId") int userId);

    @Query("SELECT ud " +
            "FROM UserDetailEntity ud " +
            "WHERE ud.userId = :userId ")
    UserDetailEntity findUserDetailByUserId(int userId);

}
