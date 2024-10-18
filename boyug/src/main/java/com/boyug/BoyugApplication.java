package com.boyug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan(basePackages = { "com.boyug.entity" })
@SpringBootApplication
public class BoyugApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoyugApplication.class, args);
	}

}
