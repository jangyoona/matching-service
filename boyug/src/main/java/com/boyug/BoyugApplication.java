package com.boyug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EntityScan(basePackages = { "com.boyug.entity" })
@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class BoyugApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoyugApplication.class, args);
	}

}
