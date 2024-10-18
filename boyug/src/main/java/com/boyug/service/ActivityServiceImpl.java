package com.boyug.service;

import com.boyug.dto.*;
import com.boyug.entity.*;
import com.boyug.repository.*;
import lombok.Setter;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class ActivityServiceImpl implements ActivityService {

    @Setter
    public SessionRepository sessionRepository;

    @Setter
    public SessionDto sessionDto;

    @Setter
    public BoyugProgramRepository boyugProgramRepository;

    @Setter
    public BoyugProgramDetailRepository boyugProgramDetailRepository;

    @Setter
    public AccountRepository accountRepository;

    @Setter
    public BoyugUserRepository boyugUserRepository;

    @Setter
    public UserSessionRepository userSessionRepository;

    @Setter
    public ProfileImageRepository profileImageRepository;

    @Setter
    public BoyugToUserRepository boyugToUserRepository;

    @Override
    public Page<BoyugProgramDto> findAllList(int page, int size) {

        // 모든 프로그램 리스트 찾기
//        List<BoyugProgramEntity> boyugProgramEntities = boyugProgramRepository.findAll();findByBoyugProgramActiveTrue

        Pageable pageable = PageRequest.of(page, size);

        int pageSize = pageable.getPageSize();

        Page<BoyugProgramEntity> boyugProgramEntities = boyugProgramRepository.findByBoyugProgramActiveTrueAndBoyugProgramIsOpenTrueOrderByBoyugProgramRegdateDesc(pageable);
//        List<BoyugProgramDto> boyugProgramList = new ArrayList<>();
//
//        // 찾은 리스트를 dto에 담기
//        for (BoyugProgramEntity entity : boyugProgramEntities) {
//            boyugProgramList.add(BoyugProgramDto.of(entity));
//        }
//        return boyugProgramList;
        // DTO 리스트로 변환
        List<BoyugProgramDto> boyugProgramList = boyugProgramEntities.stream()
                .map(BoyugProgramDto::of)
                .collect(Collectors.toList());

        return new PageImpl<>(boyugProgramList, pageable, boyugProgramEntities.getTotalElements());

    }

    @Override
    public List<BoyugProgramDto> find5Program(int userId) {

        UserEntity userEntity = accountRepository.findById(userId).get();
        UserDto user = UserDto.of(userEntity);

        double userLatitude = Double.parseDouble(user.getUserLatitude());
        double userLongitude = Double.parseDouble(user.getUserLongitude());

        // List<BoyugProgramEntity> boyugProgramEntities = boyugProgramRepository.findByBoyugProgramActiveTrueAndBoyugProgramIsOpenTrueOrderByBoyugProgramIdDesc();
        List<BoyugProgramEntity> boyugProgramEntities = boyugProgramRepository.findBoyugProgramOrderByDistanceAsc(userLatitude, userLongitude);
        List<BoyugProgramDto> boyugProgramList = new ArrayList<>();

        // Collections.shuffle(boyugProgramEntities);

        // 찾은 리스트를 dto에 담기
        int programCnt = 0;
        for (BoyugProgramEntity entity : boyugProgramEntities) {

            List<BoyugProgramDetailDto> programDetailDtos =
                    entity.getProgramDetails().stream()
                            .filter(bpd -> bpd.getBoyugProgramDetailDate().isAfter(LocalDate.now()))
                            .map(detail -> {
                                BoyugProgramDetailDto detailDto = BoyugProgramDetailDto.of(detail);
                                int utbCnt = detail.getUserToBoyugs().stream().map(utb -> utb.getRequestIsOk().equals("수락") ? 1 : 0).reduce((a, b) -> a + b).orElse(0);
                                int btuCnt = detail.getBoyugToUsers().stream().map(btu -> btu.getRequestIsOk().equals("수락") ? 1 : 0).reduce((a, b) -> a + b).orElse(0);
                                detailDto.setBoyugToUserOkCount(btuCnt);
                                detailDto.setUserToBoyugOkCount(utbCnt);
                                return detailDto;
                            })
                            .filter(detailDto -> detailDto.getProgramDetailPerson() >= detailDto.getUserToBoyugOkCount() + detailDto.getBoyugToUserOkCount())
                            .toList();

            if (!programDetailDtos.isEmpty()) {
                // DTO를 생성하고 프로그램 상세를 설정
                BoyugProgramDto boyugProgramDto = BoyugProgramDto.of(entity);
                boyugProgramDto.setProgramDetails(programDetailDtos);

                // 각 프로그램의 위도와 경도를 가져옵니다.
                double programLatitude = Double.parseDouble(boyugProgramDto.getUserLatitude()); // 프로그램의 위도
                double programLongitude = Double.parseDouble(boyugProgramDto.getUserLongitude()); // 프로그램의 경도

                // 거리 계산
                double distance = calculateDistance(userLatitude, userLongitude, programLatitude, programLongitude);
                boyugProgramDto.setDistance(distance);

                String formattedDistance = String.format("%.2f", distance);
                boyugProgramDto.setFormattedDistance(formattedDistance);

                int boyugId = boyugProgramDto.getUserId();

                // 유저 이미지
                List<ProfileImageEntity> userProfile = profileImageRepository.findProfileImageByUserUserId(boyugId);
                List<ProfileImageDto> profileImageDtos = userProfile.stream()
                        .map(ProfileImageDto::of) // 각 Entity를 DTO로 변환
                        .collect(Collectors.toList());
                boyugProgramDto.setUserImg(profileImageDtos);

                // 리스트에 추가
                boyugProgramList.add(boyugProgramDto);

                programCnt ++;
            }

            if (programCnt >= 5) {
                break;
            }

        }
        return boyugProgramList;
    }

    @Override
    public List<BoyugProgramDto> find5ProgramChltlstns(int userId) {

        UserEntity userEntity = accountRepository.findById(userId).get();
        UserDto user = UserDto.of(userEntity);

        double userLatitude = Double.parseDouble(user.getUserLatitude());
        double userLongitude = Double.parseDouble(user.getUserLongitude());

         List<BoyugProgramEntity> boyugProgramEntities = boyugProgramRepository.findByBoyugProgramActiveTrueAndBoyugProgramIsOpenTrueOrderByBoyugProgramIdDesc();
//        List<BoyugProgramEntity> boyugProgramEntities = boyugProgramRepository.findBoyugProgramOrderByDistanceAsc(userLatitude, userLongitude);
        List<BoyugProgramDto> boyugProgramList = new ArrayList<>();

        // Collections.shuffle(boyugProgramEntities);

        // 찾은 리스트를 dto에 담기
        int programCnt = 0;
        for (BoyugProgramEntity entity : boyugProgramEntities) {

            List<BoyugProgramDetailDto> programDetailDtos =
                    entity.getProgramDetails().stream()
                            .filter(bpd -> bpd.getBoyugProgramDetailDate().isAfter(LocalDate.now()))
                            .map(detail -> {
                                BoyugProgramDetailDto detailDto = BoyugProgramDetailDto.of(detail);
                                int utbCnt = detail.getUserToBoyugs().stream().map(utb -> utb.getRequestIsOk().equals("수락") ? 1 : 0).reduce((a, b) -> a + b).orElse(0);
                                int btuCnt = detail.getBoyugToUsers().stream().map(btu -> btu.getRequestIsOk().equals("수락") ? 1 : 0).reduce((a, b) -> a + b).orElse(0);
                                detailDto.setBoyugToUserOkCount(btuCnt);
                                detailDto.setUserToBoyugOkCount(utbCnt);
                                return detailDto;
                            })
                            .filter(detailDto -> detailDto.getProgramDetailPerson() >= detailDto.getUserToBoyugOkCount() + detailDto.getBoyugToUserOkCount())
                            .toList();

            if (!programDetailDtos.isEmpty()) {
                // DTO를 생성하고 프로그램 상세를 설정
                BoyugProgramDto boyugProgramDto = BoyugProgramDto.of(entity);
                boyugProgramDto.setProgramDetails(programDetailDtos);

                // 각 프로그램의 위도와 경도를 가져옵니다.
                double programLatitude = Double.parseDouble(boyugProgramDto.getUserLatitude()); // 프로그램의 위도
                double programLongitude = Double.parseDouble(boyugProgramDto.getUserLongitude()); // 프로그램의 경도

                // 거리 계산
                double distance = calculateDistance(userLatitude, userLongitude, programLatitude, programLongitude);
                boyugProgramDto.setDistance(distance);

                String formattedDistance = String.format("%.2f", distance);
                boyugProgramDto.setFormattedDistance(formattedDistance);

                int boyugId = boyugProgramDto.getUserId();

                // 유저 이미지
                List<ProfileImageEntity> userProfile = profileImageRepository.findProfileImageByUserUserId(boyugId);
                List<ProfileImageDto> profileImageDtos = userProfile.stream()
                        .map(ProfileImageDto::of) // 각 Entity를 DTO로 변환
                        .collect(Collectors.toList());
                boyugProgramDto.setUserImg(profileImageDtos);

                // 리스트에 추가
                boyugProgramList.add(boyugProgramDto);

                programCnt ++;
            }

            if (programCnt >= 5) {
                break;
            }

        }
        return boyugProgramList;
    }

    @Override
    public List<BoyugProgramDto> find5ProgramReRoll(int userId) {

        UserEntity userEntity = accountRepository.findById(userId).get();
        UserDto user = UserDto.of(userEntity);

        double userLatitude = Double.parseDouble(user.getUserLatitude());
        double userLongitude = Double.parseDouble(user.getUserLongitude());

        List<BoyugProgramEntity> boyugProgramEntities = boyugProgramRepository.findByBoyugProgramActiveTrueAndBoyugProgramIsOpenTrueOrderByBoyugProgramIdDesc();
        List<BoyugProgramDto> boyugProgramList = new ArrayList<>();

         Collections.shuffle(boyugProgramEntities);

        // 찾은 리스트를 dto에 담기
        int programCnt = 0;
        for (BoyugProgramEntity entity : boyugProgramEntities) {

            List<BoyugProgramDetailDto> programDetailDtos =
                    entity.getProgramDetails().stream()
                            .filter(bpd -> bpd.getBoyugProgramDetailDate().isAfter(LocalDate.now()))
                            .map(detail -> {
                                BoyugProgramDetailDto detailDto = BoyugProgramDetailDto.of(detail);
                                int utbCnt = detail.getUserToBoyugs().stream().map(utb -> utb.getRequestIsOk().equals("수락") ? 1 : 0).reduce((a, b) -> a + b).orElse(0);
                                int btuCnt = detail.getBoyugToUsers().stream().map(btu -> btu.getRequestIsOk().equals("수락") ? 1 : 0).reduce((a, b) -> a + b).orElse(0);
                                detailDto.setBoyugToUserOkCount(btuCnt);
                                detailDto.setUserToBoyugOkCount(utbCnt);
                                return detailDto;
                            })
                            .filter(detailDto -> detailDto.getProgramDetailPerson() >= detailDto.getUserToBoyugOkCount() + detailDto.getBoyugToUserOkCount())
                            .toList();

            if (!programDetailDtos.isEmpty()) {

                // DTO를 생성하고 프로그램 상세를 설정
                BoyugProgramDto boyugProgramDto = BoyugProgramDto.of(entity);
                boyugProgramDto.setProgramDetails(programDetailDtos);

                // 각 프로그램의 위도와 경도를 가져옵니다.
                double programLatitude = Double.parseDouble(boyugProgramDto.getUserLatitude()); // 프로그램의 위도
                double programLongitude = Double.parseDouble(boyugProgramDto.getUserLongitude()); // 프로그램의 경도

                // 거리 계산
                double distance = calculateDistance(userLatitude, userLongitude, programLatitude, programLongitude);
                boyugProgramDto.setDistance(distance);

                String formattedDistance = String.format("%.2f", distance);
                boyugProgramDto.setFormattedDistance(formattedDistance);

                int boyugId = boyugProgramDto.getUserId();

                // 유저 이미지
                List<ProfileImageEntity> userProfile = profileImageRepository.findProfileImageByUserUserId(boyugId);
                List<ProfileImageDto> profileImageDtos = userProfile.stream()
                        .map(ProfileImageDto::of) // 각 Entity를 DTO로 변환
                        .collect(Collectors.toList());
                boyugProgramDto.setUserImg(profileImageDtos);

                // 리스트에 추가
                boyugProgramList.add(boyugProgramDto);

                programCnt ++;
            }

            if (programCnt >= 5) {
                break;
            }

        }
        return boyugProgramList;
    }


    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구의 반지름 (킬로미터)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // 킬로미터
    }

    @Override
    public List<SessionDto> findAllSessions() {
        List<SessionEntity> sessionEntities = sessionRepository.findAll();

        List<SessionDto> sessions = new ArrayList<>();
        for (SessionEntity session : sessionEntities) {
            sessions.add(SessionDto.of(session));
        }
        return sessions;
    }

    @Override
    public void writeBoyugProgram(BoyugProgramDto program) {

        // 보육 프로그램 모집 글 저장
        BoyugProgramEntity programEntity = program.toEntity();

        // 해당 글에서 모집하는 활동들을 글과 연결
        List<BoyugProgramDetailEntity> detailEntities = program.getProgramDetails().stream().map((programDetail) -> {
            // session에서 sessionId로 해당 활동 찾기
            SessionEntity sessionEntity = sessionRepository.findById(programDetail.getSessionId()).get();
            // 찾기 끝

            // detailDto를 detailEntity로 변환
            BoyugProgramDetailEntity programDetailEntity = programDetail.toEntity();

            // 찾은 session을 detailEntity에 넣기
            programDetailEntity.setSession(sessionEntity);
            // 넣기 끝

            programDetailEntity.setBoyugProgram(programEntity);
            return programDetailEntity;
        }).toList();
        programEntity.setProgramDetails(detailEntities);
        // 연결 완료

        BoyugUserEntity boyugUser = boyugUserRepository.findById(program.getUserId()).orElseThrow();
        // programEntity에 조회된 userEntity 저장
        programEntity.setBoyugUser(boyugUser);

        boyugProgramRepository.save(programEntity);
        // 저장 완료

    }

    @Override
    public void editBoyugProgram(BoyugProgramDto program) {

        BoyugProgramEntity programEntity = boyugProgramRepository.findById(program.getBoyugProgramId()).get();

        programEntity.setBoyugProgramName(program.getBoyugProgramName());
        programEntity.setBoyugProgramDesc(program.getBoyugProgramDesc());
        programEntity.setBoyugProgramIsOpen(program.isBoyugProgramIsOpen());
        programEntity.setBoyugProgramModifydate(new Date());

        for (BoyugProgramDetailDto detailDto : program.getProgramDetails()) {
            if (detailDto.getBoyugProgramDetailId() != 0) {

                BoyugProgramDetailEntity detailEntity = boyugProgramDetailRepository.findById(detailDto.getBoyugProgramDetailId()).get();
                detailEntity.setBoyugProgramDetailDate(detailDto.getBoyugProgramDetailDate());
                detailEntity.setBoyugProgramDetailStartTime(detailDto.getBoyugProgramDetailStartTime());
                detailEntity.setBoyugProgramDetailEndTime(detailDto.getBoyugProgramDetailEndTime());
                detailEntity.setBoyugProgramDetailChild(detailDto.getBoyugProgramDetailChild());
                detailEntity.setProgramDetailPerson(detailDto.getProgramDetailPerson());
//            detailEntity.setBoyugProgramDetailIsOpen(detailDto.getBoyugProgramIsOpen);
                detailEntity.setBoyugProgram(programEntity);

            } else {

                SessionEntity sessionEntity = sessionRepository.findById(detailDto.getSessionId()).get();
                BoyugProgramDetailEntity detailEntity = detailDto.toEntity();
                detailEntity.setSession(sessionEntity);
                detailEntity.setBoyugProgram(programEntity);

                boyugProgramDetailRepository.save(detailEntity);

            }
        }

        boyugProgramRepository.save(programEntity);

    }

    @Override
    public BoyugProgramDto findBoyugProgram(Integer boyugProgramId) {

        Optional<BoyugProgramEntity> boyugProgramEntity = boyugProgramRepository.findById(boyugProgramId);

        // 값이 있으면서 글이 삭제되지 않은것만.
        if (boyugProgramEntity.isPresent() && boyugProgramEntity.get().isBoyugProgramActive()) {

            BoyugProgramEntity entity = boyugProgramEntity.get();

            // 조회수 + 1
            entity.setBoyugProgramCount(entity.getBoyugProgramCount() + 1);
            boyugProgramRepository.save(entity);
            // end 조회수 + 1

            BoyugProgramDto boyugProgramDto = BoyugProgramDto.of(entity);

            // 구인중인 활동들 program에 찾아서 넣기
            List<BoyugProgramDetailDto> programDetailDtos = 
                    entity.getProgramDetails().stream().map(BoyugProgramDetailDto::of).toList();

            boyugProgramDto.setProgramDetails(programDetailDtos);
            // end 찾아 넣기

            return boyugProgramDto;
        } else {
            return null;
        }

    }

    @Override
    public void insertUserToBoyug(int userId, int detailNo) {
        UserEntity user = accountRepository.findById(userId).get();
        BoyugProgramDetailEntity programDetail = boyugProgramDetailRepository.findById(detailNo).get();

        UserToBoyugEntity userToBoyug = new UserToBoyugEntity();
        userToBoyug.setBoyugProgramDetail(programDetail);
        userToBoyug.setUser(user);

        programDetail.getUserToBoyugs().add(userToBoyug);
        boyugProgramDetailRepository.save(programDetail);

    }

    @Override
    public boolean isUserApplied(int userId, int detailId) {
        return boyugProgramDetailRepository.existsByUserIdAndDetailId(userId, detailId);
    }

    @Override
    public Boolean deleteBoyugProgram(int boyugProgramId, int userId) {

        BoyugProgramEntity boyugProgram = boyugProgramRepository.findById(boyugProgramId).get();

        int writeUserId = boyugProgram.getBoyugUser().getUserId();

        if (writeUserId == userId) {

            boyugProgram.setBoyugProgramActive(false);
            boyugProgramRepository.save(boyugProgram);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<SessionDto> findUserFavorites(int userId) {
        List<SessionEntity> favoritesEntity = accountRepository.findFavoritesByUserId(userId);

        List<SessionDto> favorites = new ArrayList<>();
        for (SessionEntity session : favoritesEntity) {
            favorites.add(SessionDto.of(session));
        }

        return favorites;
    }

    @Override
    public void insertNoinRegister(int userId) {
        UserSessionEntity userSessionEntity = new UserSessionEntity();
        userSessionEntity.setUserSessionId(userId);
        userSessionEntity.setUserSessionActive(true);

        userSessionRepository.save(userSessionEntity);
    }

    @Override
    public void updateNoinUnRegister(int userId) {
        UserSessionEntity userSessionEntity = new UserSessionEntity();
        userSessionEntity.setUserSessionId(userId);
        userSessionEntity.setUserSessionActive(false);

        userSessionRepository.save(userSessionEntity);
    }

    @Override
    public boolean findRegisterUser(int userId) {

        Optional<UserSessionEntity> userSession = userSessionRepository.findByUserSessionIdAndUserSessionActive(userId, true);
        return userSession.isPresent();
    }

    @Override
    public ProfileImageDto findProfileImage(int userId) {
        Optional<UserEntity> entity = accountRepository.findById(userId);
            UserEntity userEntity = entity.get();
            UserDto user = UserDto.of(userEntity);
            List<ProfileImageDto> profiles =
                    userEntity.getImages().stream().map(ProfileImageDto::of).toList();
            user.setImages(profiles);
            if (!profiles.isEmpty()) {
                return profiles.get(0);
            } else {
                return null;
            }
    }

    @Override
    public Page<UserDto> findNoinRegisterList(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        // 등록된 노인리스트 뽑기
        Page<UserSessionEntity> userSessionEntities = userSessionRepository.findByUserSessionActiveIsTrue(pageable);

        List<UserDto> users = new ArrayList<>();

        // 등록된 노인리스트의 userId로 회원정보,선호활동 찾아 저장
        for(UserSessionEntity userSessionEntity : userSessionEntities) {
            // 회원정보
            UserEntity userEntity = accountRepository.findById(userSessionEntity.getUserSessionId()).get();
            UserDto user = UserDto.of(userEntity);

            //이미지
            List<ProfileImageDto> profiles =
                    userEntity.getImages().stream().map(ProfileImageDto::of).toList();
            user.setImages(profiles);

            // 유저 상세 정보
            UserDetailEntity userDetailEntity = accountRepository.findUserDetailByUserId(userEntity.getUserId());
            user.setUserDetail(UserDetailDto.of(userDetailEntity));

            // 선호 활동
            Set<SessionEntity> userSessionEntities1 = new HashSet<>(accountRepository.findFavoritesByUserId(userSessionEntity.getUserSessionId()));
            Set<SessionDto> favorites = new HashSet<>();
            for(SessionEntity sessionEntity : userSessionEntities1) {
                SessionDto sessionDto1 = SessionDto.of(sessionEntity);
                favorites.add(sessionDto1);
            }
            user.setFavorites(favorites);

            users.add(user);
        }

        return new PageImpl<>(users, pageable, userSessionEntities.getTotalElements());

    }

    @Override
    public UserDto findNoinRegisterUser(int userId) {
        UserEntity userEntity = accountRepository.findById(userId).get();

        Set<SessionEntity> favoritesEntity = userEntity.getFavorites();
        Set<SessionDto> favorites = new HashSet<>();
        for (SessionEntity sessionEntity : favoritesEntity) {
            SessionDto session = SessionDto.of(sessionEntity);
            favorites.add(session);
        }

        UserDetailEntity userDetailEntity = accountRepository.findUserDetailByUserId(userId);
        UserDetailDto userDetail = UserDetailDto.of(userDetailEntity);

        List<ProfileImageDto> profiles =
                userEntity.getImages().stream().map(ProfileImageDto::of).toList();

        UserDto user = UserDto.of(userEntity);
        user.setFavorites(favorites);
        user.setUserDetail(userDetail);
        user.setImages(profiles);

        return user;
    }

    @Override
    public List<BoyugProgramDetailDto> findBoyugProgramDetails(int loginUserId, int userId) {


        // 로그인한 사용자의 ID로 작성된 글을 가져옴
        List<BoyugProgramEntity> boyugProgramEntities = boyugProgramRepository.findByBoyugUser_UserId(loginUserId);

        // 글 번호를 추출
        List<Integer> programIds = boyugProgramEntities.stream()
                .map(BoyugProgramEntity::getBoyugProgramId) // getId() 메소드로 ID 추출
                .collect(Collectors.toList());

        // 글 번호로 관련된 details 검색
        // 오늘 날짜 이후의 활동들에 대해서만.
        LocalDate currentDate = LocalDate.now();
        List<BoyugProgramDetailEntity> boyugProgramDetailEntities = boyugProgramDetailRepository.findByBoyugProgram_BoyugProgramIdInAndBoyugProgramDetailDateAfter(programIds, currentDate);

        // 오래된 순으로 정렬
        List<BoyugProgramDetailEntity> sortedEntities = boyugProgramDetailEntities.stream()
                .sorted(Comparator.comparing(BoyugProgramDetailEntity::getBoyugProgramDetailDate)) // reversed() 제거
                .collect(Collectors.toList());

        // DTO 리스트로 변환
        List<BoyugProgramDetailDto> boyugProgramDetailDtos = sortedEntities.stream()
                .map(BoyugProgramDetailDto::of) // DTO로 변환하는 메소드 사용
                .collect(Collectors.toList());

        // 요청보낸 이력이 있는지 확인하는 코드
        for (BoyugProgramDetailDto boyugProgramDetailDto : boyugProgramDetailDtos) {
            boolean isRequestExists = boyugToUserRepository.existsByUserSession_UserSessionIdAndBoyugProgramDetail_BoyugProgramDetailId(userId, boyugProgramDetailDto.getBoyugProgramDetailId());
            boyugProgramDetailDto.setApplied(isRequestExists);
        }

        return boyugProgramDetailDtos;
    }

    @Override
    public void insertBoyugToUser(List<Integer> checkedValue, int userId) {

        for(int programDetailId : checkedValue) {

            BoyugProgramDetailEntity boyugProgramDetailEntity = boyugProgramDetailRepository.findById(programDetailId).get();
            UserSessionEntity userSessionEntity = userSessionRepository.findById(userId).get();

            BoyugToUserEntity boyugToUserEntity = new BoyugToUserEntity();

            boyugToUserEntity.setUserSession(userSessionEntity);
            boyugToUserEntity.setBoyugProgramDetail(boyugProgramDetailEntity);

            boyugToUserRepository.save(boyugToUserEntity);
        }
    }

    @Override
    public UserDto findUserByUserSessionId(int sessionId) { // 활동 sessionId로 유저 찾기
        Optional<UserSessionEntity> entity = userSessionRepository.findById(sessionId);
        return entity.map(userSession -> UserDto.of(userSession.getUser())).orElse(null);
    }

}
