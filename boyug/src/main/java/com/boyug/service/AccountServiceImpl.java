package com.boyug.service;

import com.boyug.common.KaKaoApi;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AccountServiceImpl implements AccountService {

    @Setter
    private AccountRepository accountRepository;

    @Setter
    private ProfileImageRepository profileImageRepository;

    @Setter
    private BoyugUserRepository boyugUserRepository;

    @Setter
    private KaKaoApi kakaoApi;

    @Value("${file.profile-dir}")
    private String profileDir;

    @Value("${file.boyugfile-dir}")
    private String boyugFileDir;

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

    //
    @Override
    public UserDto registerUserWithFiles(UserDto user, BoyugUserDto boyugUser, String domain,
                                         MultipartFile[] attach, MultipartFile attachProfile) throws IOException {
        // 위도, 경도 추출
        Map<String, Object> xy = kakaoApi.getKakaoSearch(user.getUserAddr2());
        user.setUserLongitude((String) xy.get("x"));
        user.setUserLatitude((String) xy.get("y"));

        // 이메일 설정
        String email = boyugUser.getBoyugEmail().concat("@").concat(domain);
        boyugUser.setBoyugEmail(email);
        UserDto savedUser = registerBoyugUser(user, boyugUser);

        List<BoyugUserFileDto> boyugUserFileList = new ArrayList<>();
        ProfileImageDto profileImage = new ProfileImageDto();

        if (attach.length > 0) {
            createDirectoryIfNotExists(boyugFileDir);

            for (MultipartFile file : attach) {
                String userFileName = file.getOriginalFilename();
                String savedFileName = Util.makeUniqueFileName(userFileName);

                BoyugUserFileDto userFile = new BoyugUserFileDto();
                userFile.setUserId(savedUser.getUserId());
                userFile.setFileOriginName(userFileName);
                userFile.setFileSavedName(savedFileName);
                file.transferTo(new File(boyugFileDir, savedFileName));
                boyugUserFileList.add(userFile);
            }
        }

        if (!attachProfile.isEmpty() && attachProfile.getOriginalFilename().length() > 0) {
            createDirectoryIfNotExists(profileDir);

            String profileUserFileName = attachProfile.getOriginalFilename();
            String profileSavedFileName = Util.makeUniqueFileName(profileUserFileName);

            profileImage.setUser(savedUser);
            profileImage.setImgOriginName(profileUserFileName);
            profileImage.setImgSavedName(profileSavedFileName);
            attachProfile.transferTo(new File(profileDir, profileSavedFileName));
        }

        insertAttachments(boyugUserFileList, profileImage);

        return savedUser;
    }

    private void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    //

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
