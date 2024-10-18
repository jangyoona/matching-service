package com.boyug.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.Properties;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
	
	@Bean
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource source = new ResourceBundleMessageSource();
		source.setBasenames("message.label");
		source.setDefaultEncoding("UTF-8");
		
		return source;
	}
	
	@Bean
	public StandardServletMultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver();
	}

	// Mail Config
	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.naver.com");
		mailSender.setPort(465);
		mailSender.setUsername("ddslk75");
		mailSender.setPassword("qjrrksskdwk8747!");
		mailSender.setDefaultEncoding("UTF-8");

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.debug", "false");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.auth", "true");
		props.put("mail.smtp.ssl.enable", "true");
		props.put("mail.smtp.ssl.trust", "smtp.naver.com");

		return mailSender;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 별도의 static 경로 지정
		// ( imgae, css, js, html 등 controller를 거치지 않고 요청할 수 있는 경로 )
		String currentDir = System.getProperty("user.dir");
		File uploadDir = new File(currentDir, "upload");
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}
		System.out.println("1. ----------------------> " + uploadDir.toURI().toString());
		registry
				.addResourceHandler("/board/uploads/**") // 웹 요청 경로
				.addResourceLocations(uploadDir.toURI().toString()); // static 경로
		// D:\abc\def -> file:D:\abc\def
	}

}
