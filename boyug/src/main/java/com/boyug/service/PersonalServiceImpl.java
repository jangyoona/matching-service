package com.boyug.service;

import com.boyug.common.Util;
import com.boyug.dto.*;
import com.boyug.entity.*;
import com.boyug.repository.*;
import lombok.Setter;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Collectors;

public class PersonalServiceImpl implements PersonalService{

    @Setter
    private PersonalRepository personalRepository;

    @Setter
    private PersonalDetailRepository personalDetailRepository;

    @Setter
    private SessionRepository sessionRepository;

    @Setter
    private ProfileImageRepository profileImageRepository;

    @Setter
    private BoyugUserRepository boyugUserRepository;

    @Setter
    private UserToBoyugRepository userToBoyugRepository;

    @Setter
    private BoyugProgramDetailRepository boyugProgramDetailRepository;

    @Setter
    private BoyugProgramRepository boyugProgramRepository;

    @Setter
    private BoyugToUserRepository boyugToUserRepository;

    @Setter
    private UserSessionRepository userSessionRepository;

    @Override
    public int addPersonalUser(UserDto userDto, UserDetailDto userDetailDto) {

        String hashedPasswd = Util.getHashedString(userDto.getUserPw(), "SHA-256");
        userDto.setUserPw(hashedPasswd);

        UserEntity userEntity = userDto.toEntity();

        RoleEntity role = personalRepository.findRoleByRoleName(userEntity.getUserType());
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(role);
        userEntity.setRoles(roles);

        // User insert
        UserEntity savedId = personalRepository.save(userEntity);

        // Personal User insert
        userDetailDto.setUserId(savedId.getUserId());
        if (userDetailDto.getProtectorPhone().isEmpty()) {
            userDetailDto.setProtectorPhone("0");
        }
        personalRepository.insertUserDetail(userDetailDto.getUserId(), userDetailDto.getUserBirth(),
                                            userDetailDto.getUserGender(), userDetailDto.getUserHealth(),
                                            userDetailDto.getProtectorPhone());

        return savedId.getUserId();
    }

    @Override
    public void addProfileImage(ProfileImageDto userFile) {
        personalRepository.insertProfileImage(userFile.getUserId(), userFile.getSavedDate(), userFile.getImgOriginName(), userFile.getImgSavedName());
    }

    @Override
    public String findUserPhone(String userPhone) {
        UserEntity userEntity = personalRepository.findByUserPhone(userPhone);
        return userEntity != null ? userEntity.getUserPhone() : null;
    }

    @Override
    public UserDetailDto findUserDetail(int userId) {
        UserDetailEntity userDetailEntity = personalDetailRepository.findUserDetailByUserId(userId);
        return UserDetailDto.of(userDetailEntity);
    }

    @Override
    public UserDto findUser(int userId) {
        Optional<UserEntity> entity = personalRepository.findById(userId);
        if (entity.isPresent()) {
            UserEntity userEntity = entity.get();
            UserDto user = UserDto.of(userEntity);

            List<ProfileImageDto> profiles =
                    userEntity.getImages().stream().map(ProfileImageDto::of).toList();

            Set<SessionDto> sessionDtoList =
                    userEntity.getFavorites().stream().map(SessionDto::of).collect(Collectors.toSet());

            user.setImages(profiles);
            user.setFavorites(sessionDtoList);
            return user;
        } else {
            return null;
        }
    }

    @Override
    public List<SessionDto> findAllSession() {
        List<SessionEntity> entityList = sessionRepository.findBySessionActiveTrue();
        ArrayList<SessionDto> sessionList = new ArrayList<>();
        for (SessionEntity e : entityList) {
            sessionList.add(SessionDto.of(e));
        }
        return sessionList;
    }

    @Override
    public SessionDto findSessionId(String s) {
        Optional<SessionEntity> optionalEntity = sessionRepository.findBySessionNameAndSessionActiveTrue(s);
        if (optionalEntity.isPresent()) {
            SessionEntity entity = optionalEntity.get();
            return SessionDto.of(entity);
        } else {
            return null;
        }
    }

    @Override
    public void modfiyUserAndUserDetail(UserDto user, UserDetailDto userDetail) {

        Optional<UserDetailEntity> userDetailEntityOptional = personalDetailRepository.findById(user.getUserId());
        if (userDetailEntityOptional.isPresent()) {
            UserDetailEntity existingUserDetail = userDetailEntityOptional.get();
            if (userDetail.getUserGender() == null) {
                userDetail.setUserGender(existingUserDetail.getUserGender());
            }

            UserDetailEntity userDetailEntity = userDetail.toUserDetailEntity();
            personalDetailRepository.save(userDetailEntity);
        }

        Optional<UserEntity> userEntityOptional = personalRepository.findById(user.getUserId());
        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();
            userEntity.setUserName(user.getUserName());
            userEntity.setUserPhone(user.getUserPhone());
            userEntity.setUserAddr1(user.getUserAddr1());
            userEntity.setUserAddr2(user.getUserAddr2());
            userEntity.setUserAddr3(user.getUserAddr3());

            if (user.getFavorites() != null) {
                Set<SessionEntity> sessionEntities = new HashSet<>();
                for (SessionDto sessionDto : user.getFavorites()) {
                    SessionEntity sessionEntity = sessionDto.toEntity();
                    sessionEntities.add(sessionEntity);
                }
                userEntity.setFavorites(sessionEntities);
            }

            personalRepository.save(userEntity);
        }

        if (user.getImages() != null) {
            if (user.getImages().get(0).getImageId() != 0) {
                Optional<ProfileImageEntity> profileImageEntityOptional = profileImageRepository.findById(user.getImages().get(0).getImageId());
                if (profileImageEntityOptional.isPresent()) {
                    ProfileImageEntity profileImageEntity = profileImageEntityOptional.get();
                    profileImageEntity.setImgOriginName(user.getImages().get(0).getImgOriginName());
                    profileImageEntity.setImgSavedName(user.getImages().get(0).getImgSavedName());
                    profileImageEntity.setSavedDate(user.getImages().get(0).getSavedDate());

                    profileImageRepository.save(profileImageEntity);
                }
            } else {
                ProfileImageEntity profileImageEntity = new ProfileImageEntity();
                profileImageEntity.setUser(user.toEntity());
                profileImageEntity.setImgOriginName(user.getImages().get(0).getImgOriginName());
                profileImageEntity.setImgSavedName(user.getImages().get(0).getImgSavedName());
                profileImageEntity.setSavedDate(user.getImages().get(0).getSavedDate());
                profileImageRepository.save(profileImageEntity);
            }
        }

    }

    @Override
    public List<UserDto> findBoyugUsers() {
        List<UserEntity> userEntities = personalRepository.findByUserCategoryAndUserActive(2, true);

        List<UserDto> users = userEntities.stream().map(UserDto::of).toList();

//        List<BoyugUserDto> boyugUsers = new ArrayList<>();
//
//        for (UserEntity userEntity : userEntities) {
//            Optional<BoyugUserEntity> boyugUserEntityOptional = boyugUserRepository.findById(userEntity.getUserId());
//            if (boyugUserEntityOptional.isPresent()) {
//                BoyugUserEntity boyugUserEntity = boyugUserEntityOptional.get();
//                // BoyugUserDto로 변환
//                BoyugUserDto boyugUserDto = BoyugUserDto.of(boyugUserEntity, userEntity);
//                boyugUsers.add(boyugUserDto);
//            }
//        }


//        List<BoyugUserEntity> boyugUserEntities = new ArrayList<>();
//        for (UserEntity u : userEntities) {
//            Optional<BoyugUserEntity> boyugUserEntityOptional = boyugUserRepository.findById(u.getUserId());
//            if (boyugUserEntityOptional.isPresent()) {
//                BoyugUserEntity boyugUserEntity = boyugUserEntityOptional.get();
//                boyugUserEntity.setUser(u);
//                boyugUserEntities.add(boyugUserEntity);
//            }
//        }
//
//        List<BoyugUserDto> boyugUsers = boyugUserEntities.stream().map(BoyugUserDto::of).toList();

        return users;
    }

    @Override
    public List<BoyugUserDto> findBoyugUsersDetail() {
        List<UserEntity> userEntities = personalRepository.findByUserCategoryAndUserActive(2, true);
        List<BoyugUserEntity> boyugUserEntities = new ArrayList<>();
        for (UserEntity u : userEntities) {
            Optional<BoyugUserEntity> boyugUserEntityOptional = boyugUserRepository.findById(u.getUserId());
            if (boyugUserEntityOptional.isPresent()) {
                BoyugUserEntity boyugUserEntity = boyugUserEntityOptional.get();
                boyugUserEntity.setUser(u);
                boyugUserEntities.add(boyugUserEntity);
            }
        }

        List<BoyugUserDto> boyugUsers = boyugUserEntities.stream().map(BoyugUserDto::of).toList();

        return boyugUsers;
    }

//    @Override
//    public List<BoyugProgramDetailDto> findBoyugProgramDetail(int userId) {
//        List<UserToBoyugEntity> userToBoyugEntities = userToBoyugRepository.findByUserId(userId);
//
//        List<BoyugProgramDetailDto> boyugProgramDetails = new ArrayList<>();
//        for (UserToBoyugEntity utb : userToBoyugEntities) {
//            Optional<BoyugProgramDetailEntity> entity =
//                    boyugProgramDetailRepository.findById(utb.getBoyugProgramDetail().getBoyugProgramDetailId());
//            if (entity.isPresent()) {
//                BoyugProgramDetailEntity boyugProgramDetailEntity = entity.get();
//                boyugProgramDetails.add(BoyugProgramDetailDto.of((boyugProgramDetailEntity)));
//            }
//        }
//
//        return boyugProgramDetails;
//    }

    @Override
    public List<BoyugProgramDetailDto> findBoyugProgramDetail(int userId) {
        List<UserToBoyugEntity> userToBoyugEntities = userToBoyugRepository.findByUser_UserId(userId);

        List<BoyugProgramDetailDto> boyugProgramDetails = new ArrayList<>();
        for (UserToBoyugEntity utb : userToBoyugEntities) {
            Optional<BoyugProgramDetailEntity> entity =
                    boyugProgramDetailRepository.findById(utb.getBoyugProgramDetailId());
            if (entity.isPresent()) {
                BoyugProgramDetailEntity boyugProgramDetailEntity = entity.get();
                boyugProgramDetails.add(BoyugProgramDetailDto.of(boyugProgramDetailEntity));
//                System.out.println("bpd : " +boyugProgramDetailEntity.getBoyugProgramDetailId());
//                System.out.println("bp : " +boyugProgramDetailEntity.getBoyugProgram().getBoyugProgramId());
            }
        }

//        List<BoyugProgramDto> boyugPrograms = new ArrayList<>();
//        for (BoyugProgramDetailDto dto : boyugProgramDetails) {
//            Optional<BoyugProgramEntity> entity = boyugProgramRepository.findById(dto.getBoyugProgramId());
//            if (entity.isPresent()) {
//                BoyugProgramEntity boyugProgramEntity = entity.get();
//                boyugPrograms.add(BoyugProgramDto.of(boyugProgramEntity));
//            }
//        }

        return boyugProgramDetails;
    }

    @Override
    public BoyugProgramDto findBoyugProgram(int boyugProgramId) {
        Optional<BoyugProgramEntity> entity = boyugProgramRepository.findById(boyugProgramId);
        if (entity.isPresent()) {
            BoyugProgramEntity boyugProgramEntity = entity.get();
            return BoyugProgramDto.of(boyugProgramEntity);
        } else {
            return null;
        }
//        List<BoyugProgramDto> boyugPrograms = new ArrayList<>();
//        for (BoyugProgramDetailDto dto : boyugProgramDetails) {
//            Optional<BoyugProgramEntity> entity = boyugProgramRepository.findById(dto.getBoyugProgramId());
//            if (entity.isPresent()) {
//                BoyugProgramEntity boyugProgramEntity = entity.get();
//                boyugPrograms.add(BoyugProgramDto.of(boyugProgramEntity));
//            }
//        }

    }

    @Override
    public List<BoyugProgramDetailDto> findPagingBoyugProgramDetails(int pageNo, int count) {

        Pageable pageable = PageRequest.of(pageNo,count, Sort.by(Sort.Direction.DESC, "boyugProgramDetailId"));
        Page<BoyugProgramDetailEntity> page = boyugProgramDetailRepository.findAll(pageable);
        List<BoyugProgramDetailDto> boyugProgramDetails = new ArrayList<>();
        for (BoyugProgramDetailEntity entity : page.getContent()) {
            boyugProgramDetails.add(BoyugProgramDetailDto.of(entity));
        }

        return boyugProgramDetails;
    }

    @Override
    public List<BoyugProgramDetailDto> findBoyugProgramDetailAll(int userId) {

        Optional<UserSessionEntity> userSessionEntity = userSessionRepository.findById(userId);
        if (userSessionEntity.isPresent()) {
            UserSessionEntity userSession = userSessionEntity.get();
            List<BoyugToUserEntity> boyugToUserEntities =
                    boyugToUserRepository.findByUserSession_UserSessionIdAndRequestIsOk(userSession.getUserSessionId(), "요청");

            List<BoyugProgramDetailDto> boyugProgramDetailDtos = new ArrayList<>();
            for (BoyugToUserEntity btuEntity : boyugToUserEntities) {
                BoyugToUserDto dto = BoyugToUserDto.of(btuEntity);
                BoyugProgramDetailEntity boyugProgramDetailEntity =
                        boyugProgramDetailRepository.findByBoyugProgramDetailId(dto.getBoyugProgramDetailId());
                boyugProgramDetailDtos.add(BoyugProgramDetailDto.of(boyugProgramDetailEntity));
            }

            return boyugProgramDetailDtos;
        } else {
            return null;
        }

    }

    @Override
    public List<BoyugProgramDetailDto> findPagingBoyugProgramDetailsInBTU(int pageNo, int count, int userId) {
        Pageable pageable = PageRequest.of(pageNo,count, Sort.by(Sort.Direction.ASC, "boyugProgramDetailId"));

        Page<BoyugProgramDetailEntity> page = boyugProgramDetailRepository.findByBoyugToUsers_UserSessionUserSessionIdAndBoyugToUsers_RequestIsOk(userId, pageable, "요청");

        List<BoyugProgramDetailDto> boyugProgramDetails = new ArrayList<>();
        for (BoyugProgramDetailEntity entity : page.getContent()) {
            boyugProgramDetails.add(BoyugProgramDetailDto.of(entity));
        }

        return boyugProgramDetails;
    }

    @Override
    public void modifyBoyugToUser(int userId, int programDetailId) {
        BoyugToUserEntity entity =
            boyugToUserRepository.findByUserSession_UserSessionIdAndBoyugProgramDetail_BoyugProgramDetailId(userId, programDetailId);

        entity.setRequestIsOk("수락");

        boyugToUserRepository.save(entity);
    }

    @Override
    public void modifyBoyugToUserRefuse(int userId, int programDetailId) {
        BoyugToUserEntity entity =
                boyugToUserRepository.findByUserSession_UserSessionIdAndBoyugProgramDetail_BoyugProgramDetailId(userId, programDetailId);

        entity.setRequestIsOk("거절");

        boyugToUserRepository.save(entity);
    }

    @Override
    public List<BoyugProgramDetailDto> findConfirmBoyugProgramDetailAll(int userId) {

        List<BoyugToUserEntity> boyugToUserEntities = boyugToUserRepository.findByUserSession_UserSessionIdAndRequestIsOk(userId, "수락");
        List<UserToBoyugEntity> userToBoyugEntities = userToBoyugRepository.findByUser_UserIdAndRequestIsOk(userId, "수락");
        List<BoyugProgramDetailDto> boyugProgramDetails = new ArrayList<>();

        for (BoyugToUserEntity btuEntity : boyugToUserEntities) {
            BoyugProgramDetailEntity detailEntity = btuEntity.getBoyugProgramDetail();
            BoyugProgramEntity programEntity = detailEntity.getBoyugProgram();
            detailEntity.setBoyugProgram(programEntity);
            boyugProgramDetails.add(BoyugProgramDetailDto.of(detailEntity));
        }

        for (UserToBoyugEntity utbEntity : userToBoyugEntities) {
            BoyugProgramDetailEntity detailEntity = utbEntity.getBoyugProgramDetail();
            BoyugProgramEntity programEntity = detailEntity.getBoyugProgram();
            detailEntity.setBoyugProgram(programEntity);
            boyugProgramDetails.add(BoyugProgramDetailDto.of(detailEntity));
        }

//        List<BoyugToUserEntity> boyugToUserEntities =
//                boyugToUserRepository.findByUserSession_UserSessionIdAndRequestIsOk(userId, "수락");
//
//        List<BoyugProgramDetailDto> boyugProgramDetails = new ArrayList<>();
//        for (BoyugToUserEntity btuEntity : boyugToUserEntities) {
//            Optional<BoyugProgramDetailEntity> programDetailOptionalEntity =
//                    boyugProgramDetailRepository.findById(btuEntity.getBoyugProgramDetail().getBoyugProgramDetailId());
//
//            if (programDetailOptionalEntity.isPresent()) {
//                boyugProgramDetails.add(BoyugProgramDetailDto.of(programDetailOptionalEntity.get()));
//            }
//        }

        return boyugProgramDetails;
    }

    @Override
    public List<BoyugProgramDetailDto> findPagingBoyugProgramDetailsInMatching(int pageNo, int count, int userId) {
        Pageable pageable = PageRequest.of(pageNo,count, Sort.by(Sort.Direction.ASC, "boyugProgramDetailId"));

        Page<BoyugProgramDetailEntity> page = boyugProgramDetailRepository.findByBoyugToUsers_UserSessionUserSessionIdAndBoyugToUsers_RequestIsOk(userId, pageable, "수락");

        List<BoyugProgramDetailDto> boyugProgramDetails = new ArrayList<>();
        for (BoyugProgramDetailEntity entity : page.getContent()) {
            boyugProgramDetails.add(BoyugProgramDetailDto.of(entity));
        }

        return boyugProgramDetails;
    }
}
