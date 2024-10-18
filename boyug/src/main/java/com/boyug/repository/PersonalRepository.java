package com.boyug.repository;

import com.boyug.entity.RoleEntity;
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

public interface PersonalRepository extends JpaRepository<UserEntity, Integer> {

    @Query(value = "SELECT re FROM RoleEntity re WHERE re.roleName = :roleName")
    RoleEntity findRoleByRoleName(String roleName);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO TBL_BOYUGUSER(userId, boyugUserName, boyugUserEmail, boyugUserChildNum, boyugUserConfirm)" +
            "VALUES(:userId, :boyugUserName, :boyugUserEmail, :boyugUserChildNum, :boyugUserConfirm)", nativeQuery = true)
    void insertBoyugUser(@Param("userId") int userId,
                         @Param("boyugUserName") String boyugUserName,
                         @Param("boyugUserEmail") String boyugUserEmail,
                         @Param("boyugUserChildNum") int boyugUserChildNum,
                         @Param("boyugUserConfirm") boolean boyugUserConfirm);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO TBL_USERDETAIL(userId, userBirth, userGender, userHealth, protectorPhone)" +
                   "VALUES(:userId, :userBirth, :userGender, :userHealth, :protectorPhone)", nativeQuery = true)
    void insertUserDetail(@Param("userId") int userId,
                          @Param("userBirth") Date userBirth,
                          @Param("userGender") String userGender,
                          @Param("userHealth") String userHealth,
                          @Param("protectorPhone") String protectorPhone);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO TBL_PROFILEIMAGE(imageId, userId, savedDate, imgOriginName, imgSavedName)" +
                    "VALUES(TBL_PROFILEIMAGE_SEQ.NEXTVAL, :userId, :savedDate, :imgOriginName, :imgSavedName)", nativeQuery = true)
    void insertProfileImage(int userId, Date savedDate, String imgOriginName, String imgSavedName);

    UserEntity findByUserPhone(String userPhone);

    List<UserEntity> findByUserCategoryAndUserActive(int userCategory, boolean userActive);

}
