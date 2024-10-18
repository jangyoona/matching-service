package com.boyug.service;

import com.boyug.dto.*;
import com.boyug.entity.*;
import com.boyug.repository.*;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoyugMyPageServiceImpl implements BoyugMyPageService{

    @Setter
    private BoyugProgramRepository boyugProgramRepository;

    @Setter
    private BoyugProgramDetailRepository boyugProgramDetailRepository;

    @Setter
    private BoyugToUserRepository boyugToUserRepository;

    @Setter
    private PersonalRepository personalRepository;

    @Setter
    private UserToBoyugRepository userToBoyugRepository;

    @Setter
    private ProfileImageRepository profileImageRepository;

    @Setter
    private BoyugUserRepository boyugUserRepository;

    @Override
    public List<BoyugProgramDto> findBoyugPrograms(int userId) {
        List<BoyugProgramEntity> entities = boyugProgramRepository.findByBoyugUser_UserId(userId);
        List<BoyugProgramDto> boyugProgramDtos
                = entities.stream().map(BoyugProgramDto::of).toList();

        return boyugProgramDtos;
    }

    @Override
    public List<UserToBoyugDto> findUserToBoyugs(int userId) {

        List<BoyugProgramEntity> entities = boyugProgramRepository.findByBoyugUser_UserId(userId);
        List<UserToBoyugEntity> utbEntities = new ArrayList<>();
        for (BoyugProgramEntity bpEntity : entities) {
            List<BoyugProgramDetailEntity> bpdEntities = boyugProgramDetailRepository.findByBoyugProgram_BoyugProgramId(bpEntity.getBoyugProgramId());
            for (BoyugProgramDetailEntity bpdEntity : bpdEntities) {
                UserToBoyugEntity utbEntity = userToBoyugRepository.findByBoyugProgramDetail_BoyugProgramDetailIdAndRequestIsOk(bpdEntity.getBoyugProgramDetailId(), "신청");
                if (utbEntity != null) {
                    utbEntities.add(utbEntity);
                }
            }
        }

        List<UserToBoyugDto> utbDtos = utbEntities.stream().map(UserToBoyugDto::of).toList();

        return utbDtos;
    }

    @Override
    public List<UserDto> findPersonalUsers(List<UserToBoyugDto> boyugToUsers) {
        List<UserEntity> userEntities = new ArrayList<>();

        for (UserToBoyugDto utbDto : boyugToUsers) {
            Optional<UserEntity> OptionalEntity = personalRepository.findById(utbDto.getUserId());
            if (OptionalEntity.isPresent()) {
                UserEntity entity = OptionalEntity.get();
                userEntities.add(entity);
            }
        }


//        List<UserToBoyugEntity> utbEntities = boyugToUsers.stream().map(UserToBoyugDto::toEntity).toList();
//        for (UserToBoyugEntity utbEntity : utbEntities) {
//            UserEntity user = utbEntity.getUser();
//            userEntities.add(user);
//        }

        return userEntities.stream().map(UserDto::of).toList();

    }

    @Override
    public List<BoyugProgramDetailDto> findBoyugProgramDetails(List<UserToBoyugDto> boyugToUsers) {
        List<BoyugProgramDetailEntity> bpdEntites = new ArrayList<>();

        for (UserToBoyugDto utbDto : boyugToUsers) {
            BoyugProgramDetailEntity entity = boyugProgramDetailRepository.findByBoyugProgramDetailId(utbDto.getBoyugProgramDetailId());
            bpdEntites.add(entity);
        }

        return bpdEntites.stream().map(BoyugProgramDetailDto::of).toList();

    }

    @Override
    public List<BoyugToUserDto> findBoyugToUsers(int userId) {

        List<BoyugProgramEntity> entities = boyugProgramRepository.findByBoyugUser_UserId(userId);
        List<BoyugToUserEntity> btuEntities = new ArrayList<>();
        for (BoyugProgramEntity bpEntity : entities) {
            List<BoyugProgramDetailEntity> bpdEntities = boyugProgramDetailRepository.findByBoyugProgram_BoyugProgramId(bpEntity.getBoyugProgramId());
            for (BoyugProgramDetailEntity bpdEntity : bpdEntities) {
                BoyugToUserEntity btuEntity = boyugToUserRepository.findByBoyugProgramDetail_BoyugProgramDetailId(bpdEntity.getBoyugProgramDetailId());
                if (btuEntity != null) {
                    btuEntities.add(btuEntity);
                }
            }
        }

        List<BoyugToUserDto> btuDtos = btuEntities.stream().map(BoyugToUserDto::of).toList();

        return btuDtos;
    }

    @Override
    public List<BoyugProgramDetailDto> findBoyugProgramDetailsWithBtu(List<BoyugToUserDto> boyugToUsers) {
        List<BoyugProgramDetailEntity> bpdEntites = new ArrayList<>();

        for (BoyugToUserDto btuDto : boyugToUsers) {
            BoyugProgramDetailEntity entity = boyugProgramDetailRepository.findByBoyugProgramDetailId(btuDto.getBoyugProgramDetailId());
            bpdEntites.add(entity);
        }

        return bpdEntites.stream().map(BoyugProgramDetailDto::of).toList();
    }

    @Override
    public List<UserDto> findPersonalUsersWithBtu(List<BoyugToUserDto> boyugToUsers) {
        List<UserEntity> userEntities = new ArrayList<>();

        for (BoyugToUserDto btuDto : boyugToUsers) {
            Optional<UserEntity> OptionalEntity = personalRepository.findById(btuDto.getUserSessionId());
            if (OptionalEntity.isPresent()) {
                UserEntity entity = OptionalEntity.get();
                userEntities.add(entity);
            }
        }

        return userEntities.stream().map(UserDto::of).toList();
    }

    @Override
    public void modifyUserToBoyug(int detailId, int userId) {
        UserToBoyugEntity utbEntity = userToBoyugRepository.findByBoyugProgramDetailIdAndUserId(detailId, userId);

        utbEntity.setRequestIsOk("수락");

        userToBoyugRepository.save(utbEntity);
    }

    @Override
    public void modifyUserToBoyugRefuse(int detailId, int userId) {
        UserToBoyugEntity utbEntity = userToBoyugRepository.findByBoyugProgramDetailIdAndUserId(detailId, userId);

        utbEntity.setRequestIsOk("거절");

        userToBoyugRepository.save(utbEntity);
    }

    @Override
    public List<UserToBoyugDto> findMatchingUserToBoyugs(int userId) {

//        List<BoyugProgramEntity> entities = boyugProgramRepository.findByBoyugUser_UserId(userId);
//        for (BoyugProgramEntity entity : entities) {
//
//            List<BoyugProgramDetailEntity> detailEntities = entity.getProgramDetails();
//
//            entity.setProgramDetails(detailEntities);
//        }


        List<BoyugProgramEntity> entities = boyugProgramRepository.findByBoyugUser_UserId(userId);
        List<UserToBoyugEntity> utbEntities = new ArrayList<>();
        for (BoyugProgramEntity bpEntity : entities) {
            List<BoyugProgramDetailEntity> bpdEntities = boyugProgramDetailRepository.findByBoyugProgram_BoyugProgramId(bpEntity.getBoyugProgramId());
            for (BoyugProgramDetailEntity bpdEntity : bpdEntities) {
                UserToBoyugEntity utbEntity = userToBoyugRepository.findByBoyugProgramDetail_BoyugProgramDetailIdAndRequestIsOk(bpdEntity.getBoyugProgramDetailId(), "수락");
                if (utbEntity != null) {
                    utbEntities.add(utbEntity);
                }
            }
        }

        List<UserToBoyugDto> utbDtos = utbEntities.stream().map(UserToBoyugDto::of).toList();

        return utbDtos;
    }

    @Override
    public List<BoyugProgramDetailDto> findPagingUserToBoyugs(int pageNo, int count, int userId) {

        List<BoyugProgramEntity> entities = boyugProgramRepository.findByBoyugUser_UserId(userId);
//        List<BoyugToUserEntity> btuEntities = new ArrayList<>();
//        for (BoyugProgramEntity bpEntity : entities) {
//            List<BoyugProgramDetailEntity> bpdEntities = boyugProgramDetailRepository.findByBoyugProgram_BoyugProgramId(bpEntity.getBoyugProgramId());
//            for (BoyugProgramDetailEntity bpdEntity : bpdEntities) {
//                BoyugToUserEntity btuEntity = boyugToUserRepository.findByBoyugProgramDetail_BoyugProgramDetailId(bpdEntity.getBoyugProgramDetailId());
//                if (btuEntity != null) {
//                    btuEntities.add(btuEntity);
//                }
//            }
//        }

        List<Integer> btuIds = new ArrayList<>();
        for (BoyugProgramEntity bpEntity : entities) {
            List<BoyugProgramDetailEntity> bpdEntities = boyugProgramDetailRepository.findByBoyugProgram_BoyugProgramId(bpEntity.getBoyugProgramId());
            for (BoyugProgramDetailEntity bpdEntity : bpdEntities) {
                BoyugToUserEntity btuEntity = boyugToUserRepository.findByBoyugProgramDetail_BoyugProgramDetailId(bpdEntity.getBoyugProgramDetailId());
                if (btuEntity != null) {
                    btuIds.add(btuEntity.getBoyugProgramDetail().getBoyugProgramDetailId());
                }
            }
        }
        System.out.println(btuIds);

        Pageable pageable = PageRequest.of(pageNo,count, Sort.by(Sort.Direction.ASC, "boyugProgramDetailId"));

//        Page<BoyugProgramDetailEntity> page = boyugProgramDetailRepository.findByBoyugToUsers_UserSessionUserSessionIdAndBoyugToUsers_RequestIsOk(userId, pageable, "요청");
        Page<BoyugProgramDetailEntity> page = boyugProgramDetailRepository.findByUserToBoyugs_BoyugProgramDetailIdInAndBoyugToUsers_RequestIsOk(btuIds, pageable, "신청");

        List<BoyugProgramDetailDto> boyugProgramDetails = new ArrayList<>();
        for (BoyugProgramDetailEntity entity : page.getContent()) {
            boyugProgramDetails.add(BoyugProgramDetailDto.of(entity));
        }

        return boyugProgramDetails;
    }

    @Override
    public List<BoyugProgramDto> findPagingBoyugPrograms(int pageNo, int count, int userId) {

        Pageable pageable = PageRequest.of(pageNo,count, Sort.by(Sort.Direction.ASC, "boyugProgramModifydate"));

        Page<BoyugProgramEntity> page = boyugProgramRepository.findByBoyugUser_UserId(userId, pageable);

        List<BoyugProgramDto> boyugPrograms = new ArrayList<>();
        for (BoyugProgramEntity entity : page.getContent()) {
            boyugPrograms.add(BoyugProgramDto.of(entity));
        }

        return boyugPrograms;
    }

    @Override
    public List<ProfileImageDto> findProfileImages(int userId) {

        List<ProfileImageEntity> entities = profileImageRepository.findProfileImageByUserUserId(userId);

        List<ProfileImageDto> images = entities.stream().map(ProfileImageDto::of).toList();

        return images;
    }

    @Override
    public void removeProfileImage(int imageId) {
        profileImageRepository.deleteById(imageId);
    }

    @Override
    public BoyugUserDto findBoyugUser(int userId) {

        BoyugUserEntity entity = boyugUserRepository.findByUserId(userId);

        return BoyugUserDto.of(entity);
    }

    @Override
    public void modifyBoyugUser(UserDto user, BoyugUserDto boyugUser) {
        Optional<UserEntity> userEntityOptional = personalRepository.findById(user.getUserId());
        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();
            userEntity.setUserName(user.getUserName());
            userEntity.setUserPhone(user.getUserPhone());
            userEntity.setUserAddr1(user.getUserAddr1());
            userEntity.setUserAddr2(user.getUserAddr2());
            userEntity.setUserAddr3(user.getUserAddr3());
            userEntity.setUserLongitude(user.getUserLongitude());
            userEntity.setUserLatitude(user.getUserLatitude());

            personalRepository.save(userEntity);
        }

        BoyugUserEntity entity = boyugUserRepository.findByUserId(user.getUserId());
        entity.setBoyugUserName(boyugUser.getBoyugUserName());
        entity.setBoyugUserEmail(boyugUser.getBoyugEmail());
        entity.setBoyugUserChildNum(boyugUser.getBoyugChildNum());

        boyugUserRepository.save(entity);

    }

    @Override
    public List<BoyugProgramDto> findBoyugProgramDetails2(int userId) {

//        List<BoyugProgramEntity> programEntities = boyugProgramRepository.findByBoyugUser_UserId(userId);
//
//        for (BoyugProgramEntity programEntity : programEntities) {
//            List<BoyugProgramDetailEntity> detailEntities = programEntity.getProgramDetails();
//
//            for (BoyugProgramDetailEntity detailEntity : detailEntities) {
//                List<UserToBoyugEntity> utbEntities = detailEntity.getUserToBoyugs();
//
//                for (UserToBoyugEntity utbEntity : utbEntities) {
//                    UserEntity user = utbEntity.getUser();
//                    utbEntity.setUser(user);
//                }
//
//                detailEntity.setUserToBoyugs(utbEntities);
////                UserToBoyugEntity utbEntity =
////                userToBoyugRepository.findByBoyugProgramDetail_BoyugProgramDetailIdAndRequestIsOk(detailEntity.getBoyugProgramDetailChild(), "수락");
////                if (utbEntity != null) {
////                    programEntity.setProgramDetails(detailEntities);
////                }
//            }
//
//            programEntity.setProgramDetails(detailEntities);
//
//        }

        List<BoyugProgramEntity> programEntities = boyugProgramRepository.findByBoyugUser_UserId(userId);

        ArrayList<BoyugProgramDto> programs = new ArrayList<>();
        for (BoyugProgramEntity programEntity : programEntities) {

//            List<BoyugProgramDetailDto> details =
//                programEntity.getProgramDetails().stream().map(pdEntity -> {
//                    List<UserToBoyugDto> userToBoyugDtos =
//                            pdEntity.getUserToBoyugs().stream().filter(utb -> utb.getRequestIsOk().equals("수락"))
//                                                        .map(UserToBoyugDto::of).toList();
//
//                BoyugProgramDetailDto pdDto = BoyugProgramDetailDto.of(pdEntity);
//                pdDto.setUserToBoyugs(userToBoyugDtos);
//                return pdDto;
//            }).toList();
//            BoyugProgramDto programDto = BoyugProgramDto.of(programEntity);
//            programDto.setProgramDetails(details);
//            programs.add(programDto);

            List<BoyugProgramDetailEntity> detailEntities = programEntity.getProgramDetails();
            List<BoyugProgramDetailDto> programDetailDtos = detailEntities.stream().map(BoyugProgramDetailDto::of).toList();

            for (BoyugProgramDetailEntity detailEntity : detailEntities) {
                List<UserToBoyugEntity> utbEntities = detailEntity.getUserToBoyugs().stream().filter(utb -> utb.getRequestIsOk().equals("수락")).toList();

                for (UserToBoyugEntity utbEntity : utbEntities) {
                    UserEntity user = utbEntity.getUser();
                    utbEntity.setUser(user);
                }

                detailEntity.setUserToBoyugs(utbEntities);
            }

            programEntity.setProgramDetails(detailEntities);

        }

        // return programEntities.stream().map(BoyugProgramDto::of).toList();
        return programs;
    }

    @Override
    public List<UserDto> findRequestUsers(List<BoyugProgramDto> programs) {

        List<UserEntity> users = new ArrayList<>();

        List<BoyugProgramEntity> programEntities = programs.stream().map(BoyugProgramDto::toEntity).toList();

        for (BoyugProgramEntity program : programEntities) {

            List<BoyugProgramDetailEntity> detailEntities = program.getProgramDetails();
            for (BoyugProgramDetailEntity detailEntity : detailEntities) {
                UserToBoyugEntity utbEntity =
                userToBoyugRepository.findByBoyugProgramDetail_BoyugProgramDetailIdAndRequestIsOk(detailEntity.getBoyugProgramDetailId(), "수락");
                UserEntity user = utbEntity.getUser();
                users.add(user);
            }

        }

        return users.stream().map(UserDto::of).toList();
    }

    @Override
    public List<BoyugProgramDto> findMatchingBoyugToUsers(int userId) {

        List<BoyugProgramEntity> programEntities = boyugProgramRepository.findByBoyugUser_UserId(userId);
        List<BoyugProgramDto> programDtos = programEntities.stream().map(BoyugProgramDto::of).toList();
        for (int i = 0; i < programEntities.size(); i++) {
//            List<BoyugProgramDetailEntity> detailEntities = programEntities.get(i).getProgramDetails();
//            List<BoyugProgramDetailDto> detailDtos = detailEntities.stream().map(BoyugProgramDetailDto::of).toList();
//
////            for (int j = 0; j < detailEntities.size(); j++) {
////                List<BoyugToUserEntity> btuEntities = detailEntities.get(j).getBoyugToUsers().stream().filter(btu -> btu.getRequestIsOk().equals("수락")).toList();
////                detailDtos.get(j).setBoyugToUsers(btuEntities.stream().map(BoyugToUserDto::of).toList());
////            }
//
//            programDtos.get(i).setProgramDetails(detailEntities.stream().map(BoyugProgramDetailDto::of).toList());

            List<BoyugProgramDetailDto> detailDtos = programEntities.get(i).getProgramDetails().stream().map(detailEntity -> {
                BoyugProgramDetailDto detailDto = BoyugProgramDetailDto.of(detailEntity);

                List<BoyugToUserDto> boyugToUserDtos = detailEntity.getBoyugToUsers().stream().filter(btuEntity -> btuEntity.getRequestIsOk().equals("수락")).map(BoyugToUserDto::of).toList();
                detailDto.setBoyugToUsers(boyugToUserDtos);
                List<UserToBoyugDto> userToBoyugDtos = detailEntity.getUserToBoyugs().stream().filter(utbEntity -> utbEntity.getRequestIsOk().equals("수락")).map(UserToBoyugDto::of).toList();
                detailDto.setUserToBoyugs(userToBoyugDtos);

                return detailDto;
            }).toList();

            programDtos.get(i).setProgramDetails(detailDtos);
        }

        return programDtos;
    }

//    @Override
//    public List<BoyugToUserDto> findMatchingBoyugToUsers2(List<BoyugProgramDto> programs) {
//
//        List<BoyugProgramEntity> programEntities = programs.stream().map(BoyugProgramDto::toEntity).toList();
//
//        for (BoyugProgramEntity programEntity : programEntities) {
//            for (BoyugProgramDetailEntity detailEntity : programEntity.getProgramDetails()) {
//
////                boyugToUserRepository.find
//
//            }
//        }
//
//        return List.of();
//    }

    @Override
    public List<BoyugToUserDto> findConfirmBoyugToUsers(int userId) {

        List<BoyugProgramEntity> entities = boyugProgramRepository.findByBoyugUser_UserId(userId);
        List<BoyugToUserEntity> btuEntities = new ArrayList<>();
        for (BoyugProgramEntity bpEntity : entities) {
            List<BoyugProgramDetailEntity> bpdEntities = boyugProgramDetailRepository.findByBoyugProgram_BoyugProgramId(bpEntity.getBoyugProgramId());
            for (BoyugProgramDetailEntity bpdEntity : bpdEntities) {
                BoyugToUserEntity btuEntity = boyugToUserRepository.findByBoyugProgramDetail_BoyugProgramDetailIdAndRequestIsOk(bpdEntity.getBoyugProgramDetailId(), "수락");
                if (btuEntity != null) {
                    btuEntities.add(btuEntity);
                }
            }
        }

        List<BoyugToUserDto> btuDtos = btuEntities.stream().map(BoyugToUserDto::of).toList();

        return btuDtos;
    }


}
