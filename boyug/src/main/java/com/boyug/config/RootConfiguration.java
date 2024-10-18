package com.boyug.config;


import com.boyug.repository.*;
import com.boyug.service.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement // <tx:annotation-driven 과 같은 역할
public class RootConfiguration {

	// application.properties 의 정보를 읽어서 저장하는 객체
	@Autowired
	Environment env;

	@Bean
	// Environment 객체에 저장된 속성 중에서 spring.datasource.hikari로 시작하는 속성을 적용
	@ConfigurationProperties(prefix = "spring.datasource.hikari")
	HikariConfig hikariConfig() {
		return new HikariConfig();
	}

	@Bean
	public DataSource dataSource() {
		HikariDataSource dataSource = new HikariDataSource(hikariConfig());
		return dataSource;
	}

	// security autoLogin : remember-me token DB insert
	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
		tokenRepository.setDataSource(dataSource());
		return tokenRepository;
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}

	@Bean
	public AccountService accountService(AccountRepository accountRepository, ProfileImageRepository profileImageRepository, BoyugUserRepository boyugUserRepository) throws Exception {
		AccountServiceImpl accountService = new AccountServiceImpl();
		accountService.setAccountRepository(accountRepository);
		accountService.setProfileImageRepository(profileImageRepository);
		accountService.setBoyugUserRepository(boyugUserRepository);
		return accountService;
	}

	@Bean
	public AdminService adminService(AdminRepository adminRepository, AdminUserDetailRepository adminUserDetailRepository,
									 AdminBoyugUserRepository adminBoyugUserRepository, AdminUserRepository adminUserRepository,
									 BoyugProgramRepository boyugProgramRepository, BoyugProgramDetailRepository boyugProgramDetailRepository,
									 SessionRepository sessionRepository, UserToBoyugRepository userToBoyugRepository,
									 RoleRepository roleRepository, SangDamRepository sangDamRepository,
									 UserSessionRepository userSessionRepository) throws Exception {
		AdminServiceImpl adminService = new AdminServiceImpl();
		adminService.setAdminRepository(adminRepository);
		adminService.setAdminBoyugUserRepository(adminBoyugUserRepository);
		adminService.setAdminUserRepository(adminUserRepository);
		adminService.setAdminUserDetailRepository(adminUserDetailRepository);
		adminService.setBoyugProgramRepository(boyugProgramRepository);
		adminService.setBoyugProgramDetailRepository(boyugProgramDetailRepository);
		adminService.setUserToBoyugRepository(userToBoyugRepository);
		adminService.setSessionRepository(sessionRepository);
		adminService.setRoleRepository(roleRepository);
		adminService.setSangDamRepository(sangDamRepository);
		adminService.setUserSessionRepository(userSessionRepository);
		return adminService;
	}

	@Bean BoardService boardService( BoardRepository boardRepository, BoardAttachRepository boardAttachRepository) throws Exception {
		BoardServiceImpl boardService = new BoardServiceImpl();
		boardService.setBoardRepository(boardRepository);
		boardService.setBoardAttachRepository(boardAttachRepository);
		boardService.setTransactionTemplate(transactionTemplate());
		return boardService;
	}

	@Bean
	public ActivityService activityService(SessionRepository sessionRepository,
										   BoyugProgramRepository boyugProgramRepository,
										   BoyugUserRepository boyugUserRepository,
										   BoyugProgramDetailRepository boyugProgramDetailRepository,
										   AccountRepository accountRepository,
										   UserSessionRepository userSessionRepository,
										   ProfileImageRepository profileImageRepository,
										   BoyugToUserRepository boyugToUserRepository) throws Exception {
		ActivityServiceImpl activityServiceImpl = new ActivityServiceImpl();
		activityServiceImpl.setSessionRepository(sessionRepository);
		activityServiceImpl.setBoyugProgramRepository(boyugProgramRepository);
		activityServiceImpl.setBoyugProgramDetailRepository(boyugProgramDetailRepository);
		activityServiceImpl.setBoyugUserRepository(boyugUserRepository);
		activityServiceImpl.setAccountRepository(accountRepository);
		activityServiceImpl.setUserSessionRepository(userSessionRepository);
		activityServiceImpl.setProfileImageRepository(profileImageRepository);
		activityServiceImpl.setBoyugToUserRepository(boyugToUserRepository);
		return activityServiceImpl;
	}

	@Bean
	public PersonalService personalService(PersonalRepository personalRepository, PersonalDetailRepository personalDetailRepository,
										   SessionRepository sessionRepository, ProfileImageRepository profileImageRepository,
										   BoyugUserRepository boyugUserRepository, UserToBoyugRepository userToBoyugRepository,
										   BoyugProgramDetailRepository boyugProgramDetailRepository, BoyugProgramRepository boyugProgramRepository,
										   BoyugToUserRepository boyugToUserRepository, UserSessionRepository userSessionRepository) throws Exception {
		PersonalServiceImpl personalService = new PersonalServiceImpl();
		personalService.setPersonalRepository(personalRepository);
		personalService.setPersonalDetailRepository(personalDetailRepository);
		personalService.setSessionRepository(sessionRepository);
		personalService.setProfileImageRepository(profileImageRepository);
		personalService.setBoyugUserRepository(boyugUserRepository);
		personalService.setBoyugProgramDetailRepository(boyugProgramDetailRepository);
		personalService.setUserToBoyugRepository(userToBoyugRepository);
		personalService.setBoyugProgramRepository(boyugProgramRepository);
		personalService.setBoyugToUserRepository(boyugToUserRepository);
		personalService.setUserSessionRepository(userSessionRepository);
		return personalService;
	}

	@Bean
	public BoyugMyPageService boyugMyPageService(BoyugProgramRepository boyugProgramRepository, BoyugToUserRepository boyugToUserRepository,
												 BoyugProgramDetailRepository boyugProgramDetailRepository, PersonalRepository personalRepository,
												 UserToBoyugRepository userToBoyugRepository, ProfileImageRepository profileImageRepository,
												 BoyugUserRepository boyugUserRepository) {
		BoyugMyPageServiceImpl boyugMyPageService = new BoyugMyPageServiceImpl();
		boyugMyPageService.setBoyugProgramRepository(boyugProgramRepository);
		boyugMyPageService.setBoyugToUserRepository(boyugToUserRepository);
		boyugMyPageService.setBoyugProgramDetailRepository(boyugProgramDetailRepository);
		boyugMyPageService.setPersonalRepository(personalRepository);
		boyugMyPageService.setUserToBoyugRepository(userToBoyugRepository);
		boyugMyPageService.setProfileImageRepository(profileImageRepository);
		boyugMyPageService.setBoyugUserRepository(boyugUserRepository);
		return boyugMyPageService;
	}

	@Bean
	public ChattingService chattingService(ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository) throws Exception{
		ChattingServiceImpl chattingService = new ChattingServiceImpl();
		chattingService.setChatRoomRepository(chatRoomRepository);
		chattingService.setChatMessageRepository(chatMessageRepository);
		return chattingService;
	}

	@Bean
	public NotificationService notificationService(NotificationRepository notificationRepository) throws Exception {
		NotificationServiceImpl notificationService = new NotificationServiceImpl();
		notificationService.setNotificationRepository(notificationRepository);
		return notificationService;
	}

	@Bean
	public BookmarkService bookmarkService(BookmarkRepository bookmarkRepository) throws Exception {
		BookmarkServiceImpl bookmarkService = new BookmarkServiceImpl();
		bookmarkService.setBookmarkRepository(bookmarkRepository);
		return bookmarkService;
	}

	@Bean PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(dataSource());
		return transactionManager;
	}
	
	@Bean TransactionTemplate transactionTemplate() {
		TransactionTemplate transactionTemplate = new TransactionTemplate();
		transactionTemplate.setTransactionManager(transactionManager());
		return transactionTemplate;
	}

	@Bean
	SangDamService sangDamService(SangDamRepository sangDamRepository) throws Exception {
		SangDamServiceImpl sangDamService = new SangDamServiceImpl();
		sangDamService.setSangDamRepository(sangDamRepository);
		return sangDamService;
	}
}







