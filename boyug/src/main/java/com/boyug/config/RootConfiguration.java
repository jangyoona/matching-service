package com.boyug.config;


import com.boyug.common.KaKaoApi;
import com.boyug.repository.*;
import com.boyug.service.*;
import com.boyug.websocket.SocketHandler;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
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

	@Value("${spring.data.redis.host}")
	private String redisHost;

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

	// redisTemplate
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, 6379);
		return new LettuceConnectionFactory(config);
	}

	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) throws Exception {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// 키와 값을 String 형식으로 직렬화 설정
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new StringRedisSerializer());

		// 설정 초기화
//		template.afterPropertiesSet();
		return template;
	}

	// Cache 직렬화 설정
	@Bean
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory) throws Exception{
		RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisTemplate(connectionFactory).getConnectionFactory());

		// 기본 캐시 설정 (직렬화 방식 설정)
		builder.cacheDefaults(
				RedisCacheConfiguration.defaultCacheConfig()
						.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
		);

		return builder.build();
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer() {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory()); // 팩토리 설정
		return container;
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
	public AdminService adminService(AdminRepository adminRepository, AdminUserDetailRepository adminUserDetailRepository,
									 AdminBoyugUserRepository adminBoyugUserRepository, AdminUserRepository adminUserRepository,
									 BoyugProgramRepository boyugProgramRepository, BoyugProgramDetailRepository boyugProgramDetailRepository,
									 SessionsRepository sessionsRepository, UserToBoyugRepository userToBoyugRepository,
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
		adminService.setSessionsRepository(sessionsRepository);
		adminService.setRoleRepository(roleRepository);
		adminService.setSangDamRepository(sangDamRepository);
		adminService.setUserSessionRepository(userSessionRepository);
		return adminService;
	}

	@Bean BoardService boardService( BoardRepository boardRepository, BoardAttachRepository boardAttachRepository, EntityManagerFactory entityManagerFactory) throws Exception {
		BoardServiceImpl boardService = new BoardServiceImpl();
		boardService.setBoardRepository(boardRepository);
		boardService.setBoardAttachRepository(boardAttachRepository);
		boardService.setTransactionTemplate(transactionTemplate(entityManagerFactory));
		return boardService;
	}

	@Bean
	public ActivityService activityService(SessionsRepository sessionsRepository,
										   BoyugProgramRepository boyugProgramRepository,
										   BoyugUserRepository boyugUserRepository,
										   BoyugProgramDetailRepository boyugProgramDetailRepository,
										   AccountRepository accountRepository,
										   UserSessionRepository userSessionRepository,
										   ProfileImageRepository profileImageRepository,
										   BoyugToUserRepository boyugToUserRepository) throws Exception {
		ActivityServiceImpl activityServiceImpl = new ActivityServiceImpl();
		activityServiceImpl.setSessionsRepository(sessionsRepository);
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
										   SessionsRepository sessionsRepository, ProfileImageRepository profileImageRepository,
										   BoyugUserRepository boyugUserRepository, UserToBoyugRepository userToBoyugRepository,
										   BoyugProgramDetailRepository boyugProgramDetailRepository, BoyugProgramRepository boyugProgramRepository,
										   BoyugToUserRepository boyugToUserRepository, UserSessionRepository userSessionRepository) throws Exception {
		PersonalServiceImpl personalService = new PersonalServiceImpl();
		personalService.setPersonalRepository(personalRepository);
		personalService.setPersonalDetailRepository(personalDetailRepository);
		personalService.setSessionsRepository(sessionsRepository);
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

	// JPA 트랜잭션
	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

//	@Bean PlatformTransactionManager transactionManager() { //JDBC?
//		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
//		transactionManager.setDataSource(dataSource());
//		return transactionManager;
//	}
	
	@Bean TransactionTemplate transactionTemplate(EntityManagerFactory entityManagerFactory) {
		TransactionTemplate transactionTemplate = new TransactionTemplate();
		transactionTemplate.setTransactionManager(transactionManager(entityManagerFactory));
		return transactionTemplate;
	}
}







