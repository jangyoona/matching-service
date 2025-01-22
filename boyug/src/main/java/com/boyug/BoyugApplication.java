package com.boyug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EntityScan(basePackages = { "com.boyug.entity" })
@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableTransactionManagement
public class BoyugApplication {

	public static void main(String[] args) {

		// Spring Environment 객체에 Env 파일 내용 전체 등록
//		Dotenv dotenv = Dotenv.configure().load();
//		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		// 개별 등록
		// System.setProperty("DB_URL", dotenv.get("DB_URL"));

		SpringApplication.run(BoyugApplication.class, args);
	}

}
