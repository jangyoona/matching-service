package com.boyug.service;

import com.boyug.dto.*;
import com.boyug.entity.*;
import com.boyug.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class AdminServiceImpl implements AdminService {

    @Setter
    private AdminRepository adminRepository;
    @Setter
    private AdminUserRepository adminUserRepository;
    @Setter
    private AdminBoyugUserRepository adminBoyugUserRepository;
    @Setter
    private AdminUserDetailRepository adminUserDetailRepository;
    @Setter
    private BoyugProgramRepository boyugProgramRepository;
    @Setter
    private BoyugProgramDetailRepository boyugProgramDetailRepository;
    @Setter
    private UserToBoyugRepository userToBoyugRepository;
    @Setter
    private RoleRepository roleRepository;
    @Setter
    private SessionRepository sessionRepository;
    @Setter
    private SangDamRepository sangDamRepository;
    @Setter
    private UserSessionRepository userSessionRepository;



//    @Override
//    public List<BoyugUserDto> findAllUser() {
//        List<BoyugUserEntity> userEntities = adminRepository.findAll();
//        List<BoyugUserDto> users = userEntities.stream().map(BoyugUserDto::of).toList();
//        return users;
//    }
//    @Override
//    public List<BoyugUserDto> findAllBoyug() {
//        // List<BoyugUserEntity> boyugUsers = adminRepository.selectBU();
//        List<BoyugUserEntity> boyugUsers = adminRepository.findAll();
//        List<BoyugUserDto> boyugs = boyugUsers.stream().map(BoyugUserDto::of).toList();
//        return boyugs;
//    }

    public int getBoyugUserCount() {
        return (int) adminBoyugUserRepository.count(); // 보육원 회원 수를 카운트하는 로직
    }

    public int getUserDetailCount() {
        return (int) adminUserDetailRepository.count(); // 개인 회원 수를 카운트하는 로직
    }

    public int getBoyugProgramDetailCount() {
        return (int) boyugProgramDetailRepository.count();
    }

    public int getBoyugProgramCount() {
        return (int) boyugProgramRepository.count();
    }

    @Override
    public List<UserDetailDto> findAllUser() {
        List<UserDetailEntity> userDetailEntities = adminUserDetailRepository.findAll();
        List<UserDetailDto> users = userDetailEntities.stream()
                .map(userEntity -> UserDetailDto.of(userEntity, userEntity.getUser()))
                .toList();
        return users;
    }

    @Override
    public List<BoyugUserDto> findAllBoyug() {
        List<BoyugUserEntity> userEntities = adminBoyugUserRepository.findAll();
        List<BoyugUserDto> boyugs = userEntities.stream()
                .map(userEntity -> BoyugUserDto.of(userEntity, userEntity.getUser()))
                .toList();
        return boyugs;
    }

//    @Override
//    public List<BoyugUserDto> findAllBoyug() {
//        List<Object[]> boyugUsers = adminRepository.selectBU();  // BoyugUserEntity와 UserEntity를 함께 가져옴
//        return boyugUsers.stream()
//                .map(objects -> {
//                    BoyugUserEntity boyugUserEntity = (BoyugUserEntity) objects[0];
//                    UserEntity userEntity = (UserEntity) objects[1];
//                    return BoyugUserDto.of(boyugUserEntity, userEntity);  // BoyugUserDto.of에서 두 엔티티를 사용하여 DTO 생성
//                })
//                .toList();
//    }

    public void boyugcomfirm(int userId) {

        int updatedRows = adminBoyugUserRepository.updateConfirm(userId);
        if (updatedRows == 0) {
            throw new RuntimeException("이번호인 보육원을 찾을 수 없습니다.");
        }
    }

    public void updateHealthStatus(int userId, String userHealth) {
        UserDetailEntity user = adminUserDetailRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        user.setUserHealth(userHealth);  // UserDetailEntity에서 건강상태를 업데이트
        adminUserDetailRepository.save(user);  // 변경 사항을 저장
    }

    @Override
    public void updateProtectorPhone(int userId, String protectorPhone) {
        UserDetailEntity user = adminUserDetailRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        user.setProtectorPhone(protectorPhone);  // 보호자연락처 업데이트
        adminUserDetailRepository.save(user);
    }

    @Override
    public UserEntity findByUserId(int userId) {
        return adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Override
    public void saveuser(UserEntity user) {
        adminUserRepository.save(user);
    }

    @Override
    public UserDetailEntity findById(int userId) {
        return adminUserDetailRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Override
    public void save(UserDetailEntity user) {
        adminUserDetailRepository.save(user);
    }

    @Override
    public boolean updateUserName(int userId, String userName) {
        UserEntity user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

        user.setUserName(userName);  // 유저 이름 업데이트
        adminUserRepository.save(user);  // 변경사항 저장
        return true;
    }

    @Override
    public void updateBoyugChildNum(int userId, int boyugChildNum){
        BoyugUserEntity boyugUser = adminBoyugUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        boyugUser.setBoyugUserChildNum(boyugChildNum);  // 보호자연락처 업데이트
        adminBoyugUserRepository.save(boyugUser);

    }

    @Override
    public void updateBoyugEmail(int userId, String boyugEmail){
        BoyugUserEntity boyugUser = adminBoyugUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        boyugUser.setBoyugUserEmail(boyugEmail);  // 보육원이메일 업데이트
        adminBoyugUserRepository.save(boyugUser);
    }

    @Override
    public void updateBoyugName(int userId, String boyugName) {
        BoyugUserEntity boyugUser = adminBoyugUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        boyugUser.setBoyugUserName(boyugName);  // 보육원이메일 업데이트
        adminBoyugUserRepository.save(boyugUser);
    }

    public void updateUserBirth(int userId, Date birthDate) {
        // 사용자 ID로 UserDetailEntity 조회
        UserDetailEntity user = adminUserDetailRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 생일 업데이트
        user.setUserBirth(birthDate); // userBirth는 UserDetailEntity의 생일 필드
        adminUserDetailRepository.save(user); // 변경 사항 저장
    }


    public BoyugUserDto getBoyugUserById(int userId) {
        return adminBoyugUserRepository.findById(userId)
                .map(userEntity -> BoyugUserDto.of(userEntity, userEntity.getUser()))
                .orElse(null);  // Optional 값이 없을 경우 null 반환
    }


    public List<BoyugProgramDto> getBoyugProgramsByUserId(int userId) {

        // 유저에 해당하는 보육 프로그램 엔티티 리스트 가져오기
        List<BoyugProgramEntity> boyugProgramEntities = boyugProgramRepository.findByBoyugUser_UserId(userId);

        // 각 프로그램에 해당하는 디테일 리스트를 매핑하여 BoyugProgramDto로 변환
        List<BoyugProgramDto> boyugProgramDtos = boyugProgramEntities.stream()
                .map(boyugProgramEntity -> {
                    List<BoyugProgramDetailEntity> boyugProgramDetailEntities =
                            boyugProgramDetailRepository.findByBoyugProgram_BoyugProgramId(boyugProgramEntity.getBoyugProgramId());

                    // BoyugProgramDetailEntity 리스트를 DTO 리스트로 변환
                    List<BoyugProgramDetailDto> boyugProgramDetailDtos = boyugProgramDetailEntities.stream()
                            .map(BoyugProgramDetailDto::of)
                            .collect(Collectors.toList());

                    // BoyugProgramDto 생성 시 programDetails 포함
                    BoyugProgramDto boyugProgramDto = BoyugProgramDto.of(boyugProgramEntity);
                    boyugProgramDto.setProgramDetails(boyugProgramDetailDtos);

                    return boyugProgramDto;
                })
                .collect(Collectors.toList());

        return boyugProgramDtos;
    }

    public List<BoyugProgramDto> getAllBoyugPrograms() {
        // 데이터베이스에서 가져온 데이터를 DTO로 변환 후 반환
        return boyugProgramRepository.findAll().stream()
                .map(BoyugProgramDto::of)
                .collect(Collectors.toList());
    }

    public List<Object[]> getUserCountBySession() {
        return adminUserRepository.countUsersBySession();
    }

    public List<Object[]> getStatisticsByDate() {
        return boyugProgramRepository.getProgramsByDate();

    }

    public List<Object[]> getDetailsByDate() {
        return boyugProgramDetailRepository.getDetailsByDate();

    }

    @Override
    public UserDetailDto findUserDetailById(int userId) {
        Optional<UserDetailEntity> userDetailEntityOptional = adminUserDetailRepository.findById(userId);

        // Optional이 비어있을 경우 예외 처리
        UserDetailEntity userDetailEntity = userDetailEntityOptional
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다 : " + userId));

        // UserDetailDto로 변환
        return UserDetailDto.of(userDetailEntity, userDetailEntity.getUser());
    }

    @Override
    public List<UserToBoyugDto> findUserToBoyugByUserId(Integer userId) {

        // userId를 기반으로 UserToBoyugEntity 리스트를 조회
        List<UserToBoyugEntity> entities = userToBoyugRepository.findByUser_UserId(userId);

        // 각 UserToBoyugEntity를 UserToBoyugDto로 변환하여 리스트로 반환
        return entities.stream()
                .map(UserToBoyugDto::of) // 각 UserToBoyugEntity를 UserToBoyugDto로 변환
                .collect(Collectors.toList());
    }

    public UserDto findUserWithRolesById(Integer userId) {
        UserEntity user = adminUserRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // return UserDto.of(user);  // UserDto로 변환
        UserDto userDto = UserDto.of(user);
        userDto.setRoles(new HashSet<RoleDto>(user.getRoles().stream().map(RoleDto::of).toList()));
        return userDto;
    }

    public void addRoleToUser(Integer userId, String roleName) {
        UserEntity user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        RoleEntity role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("ADMIN을 찾을 수 없습니다."));

        user.getRoles().add(role);
        adminUserRepository.save(user);
    }

    public void removeRoleFromUser(Integer userId, String roleName) {
        UserEntity user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        RoleEntity role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("ADMIN을 찾을 수 없습니다."));

        user.getRoles().remove(role);
        adminUserRepository.save(user);
    }
    
    // 모든 세션
    @Override
    public List<SessionDto> findAllSession() {
        return sessionRepository.findAll().stream()
                .map(SessionDto::of)
                .collect(Collectors.toList());
    }

    // 세션 추가
    public void addSession(SessionEntity sessionEntity) {
        sessionRepository.save(sessionEntity);
    }

    // 세션 삭제
    public void deleteSession(int sessionId) {
        sessionRepository.deleteById(sessionId);
    }
    // 엑티브 변경
    public void updateSessionActive(int sessionId, boolean isActive) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("세션을 찾을 수 없습니다."));
        session.setSessionActive(isActive);
        sessionRepository.save(session);
    }
    // 세션 이름 업데이트 메소드
    public void updateSessionName(int sessionId, String newSessionName) {
        // 세션 ID로 세션 엔티티 조회
        Optional<SessionEntity> sessionOptional = sessionRepository.findById(sessionId);

        if (sessionOptional.isPresent()) {
            SessionEntity session = sessionOptional.get();
            session.setSessionName(newSessionName); // 새 이름 설정
            sessionRepository.save(session); // 변경 사항 저장
        } else {
            throw new EntityNotFoundException("아이디로 세션을찾을 수 없습니다.: " + sessionId);
        }
    }

    @Override
    public List<UserSessionDto> findAllUserSession() {
        return userSessionRepository.findAll().stream()
                .map(UserSessionDto::of)
                .collect(Collectors.toList());
    }

    public List<SangdamDto> findAllSangdam() {

        return sangDamRepository.findAll().stream()
                .map(SangdamDto::of)
                .collect(Collectors.toList());
    }
    @Transactional
    public void updateSangdamActive(int sangdamId, String sangdamActive) {
        SangdamEntity sangdam = sangDamRepository.findById(sangdamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담 ID가 존재하지 않습니다: " + sangdamId));
        sangdam.setSangdamActive(sangdamActive);
        sangDamRepository.save(sangdam);
        sangDamRepository.flush();
    }

    public void updateProgramStatus(int boyugProgramId, boolean boyugProgramActive) {
        Optional<BoyugProgramEntity> optionalProgram = boyugProgramRepository.findById(boyugProgramId);
        if (optionalProgram.isPresent()) {
            BoyugProgramEntity program = optionalProgram.get();
            program.setBoyugProgramActive(boyugProgramActive);
            boyugProgramRepository.save(program);
        } else {
            throw new EntityNotFoundException("프로그램을 찾을 수 없습니다. ID: " + boyugProgramId);
        }
    }

    public void updateProgramDetailStatus(int boyugProgramDetailId, boolean boyugProgramDetailActive) {
        Optional<BoyugProgramDetailEntity> optionalProgram = boyugProgramDetailRepository.findById(boyugProgramDetailId);
        if (optionalProgram.isPresent()) {
            BoyugProgramDetailEntity detail = optionalProgram.get();
            detail.setBoyugProgramDetailActive(boyugProgramDetailActive);
            boyugProgramDetailRepository.save(detail);
        } else {
            throw new EntityNotFoundException("프로그램디테일을 찾을 수 없습니다. ID: " + boyugProgramDetailId);
        }

    }

    // 유저세션 엑티브 변경
    public void updateUserSessionActive(int userSessionId, boolean isActive) {
        UserSessionEntity userSession = userSessionRepository.findById(userSessionId)
                .orElseThrow(() -> new EntityNotFoundException("세션을 찾을 수 없습니다."));
        userSession.setUserSessionActive(isActive);
        userSessionRepository.save(userSession);
    }

}
