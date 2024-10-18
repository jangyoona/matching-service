package com.boyug.service;

import com.boyug.common.Util;
import com.boyug.dto.BoyugUserDto;
import com.boyug.dto.BoyugUserFileDto;
import com.boyug.dto.ProfileImageDto;
import com.boyug.dto.UserDto;
import com.boyug.entity.BoyugUserEntity;
import com.boyug.entity.ProfileImageEntity;
import com.boyug.entity.RoleEntity;
import com.boyug.entity.UserEntity;
import com.boyug.repository.AccountRepository;
import com.boyug.repository.BoyugUserRepository;
import com.boyug.repository.ProfileImageRepository;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AccountServiceImpl implements AccountService {

    @Setter
    private AccountRepository accountRepository;

    @Setter
    private ProfileImageRepository profileImageRepository;

    @Setter
    private BoyugUserRepository boyugUserRepository;

    @Override
    public UserDto getUserInfo(int userId) {
        Optional<UserEntity> entity = accountRepository.findById(userId);
        return entity.isPresent() ? UserDto.of(entity.get()) : null;
    }

    @Override
    public BoyugUserDto getBoyugUserInfo(int userId) {
        Optional<BoyugUserEntity> entity = boyugUserRepository.findById(userId);
        return entity.isPresent() ? BoyugUserDto.of(entity.get()) : null;
    }


    @Override
    public long dupCheckUserPhone(String userPhone) {
        return accountRepository.countByUserPhone(userPhone);
    }

    @Override
    public int getSocialIdByUserPhone(String userPhone) {
        return accountRepository.countByUserPhoneAndSocialIdIsNotNull(userPhone);
    }

    @Transactional
    @Override
    public UserDto registerBoyugUser(UserDto user, BoyugUserDto boyugUser) {

        // PassWord 암호화
        String hashedPasswd = Util.getHashedString(user.getUserPw(), "SHA-256");
        user.setUserPw(hashedPasswd);

        UserEntity userEntity = user.toEntity();

        // User Type 확인 후 저장
        RoleEntity role = accountRepository.findRoleByRoleName(userEntity.getUserType());
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(role);
        userEntity.setRoles(roles);

        // User Table insert
        UserEntity savedUser = accountRepository.save(userEntity);

        // boyugUser Table insert
        boyugUser.setUserId(savedUser.getUserId());
        boyugUserRepository.save(boyugUser.toEntity());

        return UserDto.of(savedUser);
    }

    @Transactional
    @Override
    public void insertAttachments(List<BoyugUserFileDto> userFile, ProfileImageDto profileImage) {

        for (BoyugUserFileDto boyugUserFileDto : userFile) {
            accountRepository.insertBoyugUserFile(boyugUserFileDto.getUserId(),boyugUserFileDto.getFileOriginName(),
                                                  boyugUserFileDto.getFileSavedName(),boyugUserFileDto.getSavedDate());
        }
        profileImageRepository.save(profileImage.toEntity());
    }

    @Override
    public boolean updateUserPasswd(String userPhone, String userPw) {
        return accountRepository.findByUserPhone(String.valueOf(userPhone))
                .map(entity -> {
                    String hashedPasswd = Util.getHashedString(userPw, "SHA-256");
                    entity.setUserPw(hashedPasswd);
                    accountRepository.save(entity);
                    return true;
                })
                .orElseGet(() -> {
                    return false;
                });
    }
    @Override
    public List<ProfileImageDto> getUserProfileImage(UserDto user) {
        List<ProfileImageEntity> entity = profileImageRepository.findProfileImageByUserUserId(user.getUserId());
        return entity.stream().map(ProfileImageDto::of).collect(Collectors.toList());
    }


}
