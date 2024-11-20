package com.boyug.config;

import com.boyug.oauth2.CustomAuthenticationFailureHandler;
import com.boyug.oauth2.CustomOAuth2UserService;
import com.boyug.security.LoginSuccessHandler;
import com.boyug.security.LogoutSuccessHandler;
import com.boyug.security.WebPasswordEncoder;
import com.boyug.security.WebUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public CustomOAuth2UserService customOAuth2UserService() {
        return new CustomOAuth2UserService();
    }

    @Bean
    public CustomAuthenticationFailureHandler customAuthenticationFailureHandler() { return new CustomAuthenticationFailureHandler(); }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() { return new LogoutSuccessHandler(); }

    @Autowired
    private PersistentTokenRepository persistentTokenRepository;

    private static final String rememberMeKey = "project3";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/login-denied").permitAll()
                        .requestMatchers("/board/*list*","/board/*write*", "/board/*edit*", "/board/*delete*","/board/*detail*").authenticated() // authenticated? 로그인한 사용자만 허용 설정
                        .requestMatchers("/chat/**", "/moveChatting/**").authenticated()
                        .requestMatchers("/userView/activityPages/*noinRegisterDetail*").hasAnyRole("BOYUG", "ADMIN")
                        .requestMatchers("/myPage/**").hasRole("BOYUG")
                        .requestMatchers("/adminView/**", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/personal/personalHome").hasRole("USER")
                        .anyRequest().permitAll()) // 그 외 모든 요청은 허용
                .sessionManagement(session -> session
                        .maximumSessions(1)) // 동시 세션 제어
//                                .exceptionHandling(exception -> exception
//                                        .accessDeniedPage("/login-denied") // 권한 부족시 404 페이지
////                        .maxSessionsPreventsLogin(true) // 동시 로그인 차단
//                )
                                .exceptionHandling(configurer -> configurer.accessDeniedHandler(accessDeniedHandler())) // 권한 부족시 404 페이지
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin((login) -> login
                        .loginPage("/userView/account/login")
                        .usernameParameter("userPhone")
                        .passwordParameter("userPw")
                        .successHandler(loginSuccessHandler())
                        .loginProcessingUrl("/userView/account/process-login")
                )

                .logout((logout) -> logout
                        .logoutUrl("/userView/account/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .invalidateHttpSession(true) // 로그아웃시 로그인 했던 모든 정보를 삭제
                        .deleteCookies("JSESSIONID")); // 톰캣이 만든 세션Id

        // OAuth2
        http
                .oauth2Login(OAuth2ClientConfigurer -> OAuth2ClientConfigurer
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                        .userService(customOAuth2UserService()))
                        .defaultSuccessUrl("/personal/personalHome", true)
                        .failureHandler(customAuthenticationFailureHandler()));
        // remember-me
        http.rememberMe( rememberMe -> rememberMe
                .key(rememberMeKey)
                .tokenRepository(persistentTokenRepository)
                .tokenValiditySeconds(1209600) // 토큰 유효기간 14일
                .authenticationSuccessHandler(loginSuccessHandler())
                .alwaysRemember(false)
                .userDetailsService(userDetailsService())
        );
//        // 주로 인증 실패나 권한이 없는 접근 시에 호출되는 핸들러
//                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(customAuthenticationEntryPoint()))

        return http.build();
    }

    private AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.sendRedirect("/home"); // Redirect to your custom URL
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // 권한 부족 시 리다이렉트
            response.sendRedirect("/login-denied");
        };
    }


    // Custom Encoder
    @Bean
    PasswordEncoder passwordEncoder() {
        return new WebPasswordEncoder();
    }

    // UserDetails(Dto역할) + Service + PasswordEncoder Custom
    @Bean
    public UserDetailsService userDetailsService() {
        return new WebUserDetailsService();
    }

}
