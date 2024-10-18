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
                        .requestMatchers("/admin/adminView/login-denied").permitAll()
                        .requestMatchers("/board/*list*","/board/*write*", "/board/*edit*", "/board/*delete*","/board/*detail*").authenticated() // authenticated? 로그인한 사용자만 허용 설정
                        .requestMatchers("/chat/**", "/moveChatting/**").authenticated() // authenticated? 로그인한 사용자만 허용 설정
                        .requestMatchers("/userView/activityPages/*noinRegisterDetail*").hasAnyRole("BOYUG", "ADMIN") // authenticated? 로그인한 사용자만 허용 설정
                        .requestMatchers("/myPage/**").hasRole("BOYUG")
                        .requestMatchers("/adminView/**", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/personal/personalHome").hasRole("USER")
                        .anyRequest().permitAll()) // 그 외 모든 요청은 허용
                .sessionManagement(session -> session
                        .maximumSessions(2)) // 동시 세션 제어
                                .exceptionHandling(exception -> exception
                                        .accessDeniedPage("/admin/adminView/login-denied")
//                        .maxSessionsPreventsLogin(true) // 동시 로그인 차단
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin((login) -> login
                        .loginPage("/userView/account/login")
                        .usernameParameter("userPhone")
                        .passwordParameter("userPw")
                        .successHandler(loginSuccessHandler())
                        .loginProcessingUrl("/userView/account/process-login")
//                        .failureHandler(loginFailHandler())
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/admin/adminView/login-denied")) // ADMIN 아닐 경우 리다이렉트 URL
                         // 로그인 실패 시 보낼 경로
//                        .loginProcessingUrl("/userView/account/process-login").defaultSuccessUrl("/home",true))

                .logout((logout) -> logout
                        .logoutUrl("/userView/account/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .invalidateHttpSession(true) // 로그아웃시 로그인 했던 모든 정보를 삭제
                        .deleteCookies("JSESSIONID") // 톰캣이 만든 세션Id
                        .logoutSuccessUrl("/home")); // 로그아웃시 리다이렉트할 URL
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
        )
                // 주로 인증 실패나 권한이 없는 접근 시에 호출되는 핸들러
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(customAuthenticationEntryPoint()));
        return http.build();
    }

    private AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.sendRedirect("/home"); // Redirect to your custom URL
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
