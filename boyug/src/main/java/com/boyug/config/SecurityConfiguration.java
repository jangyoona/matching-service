package com.boyug.config;

import com.boyug.oauth2.CustomAuthenticationFailureHandler;
import com.boyug.oauth2.CustomOAuth2UserService;
import com.boyug.security.LoginSuccessHandler;
import com.boyug.security.LogoutSuccessHandler;
import com.boyug.security.WebPasswordEncoder;
import com.boyug.security.jwt.CustomAccessDeniedHandler;
import com.boyug.security.jwt.CustomAuthenticationEntryPoint;
import com.boyug.security.jwt.JwtAuthFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {

//    private final UserDetailsService userDetailsService;

    private final LoginSuccessHandler loginSuccessHandler;
    private final LogoutSuccessHandler logoutSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

//    private final PersistentTokenRepository persistentTokenRepository;

    // jwt
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint jwtAuthenticationEntryPoint;

//    private static final String rememberMeKey = "project3";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
//                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/login-denied").permitAll()
                        .requestMatchers("/board/**").authenticated() // authenticated? 로그인한 사용자만 허용 설정
                        .requestMatchers("/chat/**", "/moveChatting/**").authenticated()
                        .requestMatchers("/userView/activityPages/*noinRegisterDetail*", "/userView/activityPages/*noinRegisterList*").hasAnyRole("BOYUG", "ADMIN")
                        .requestMatchers("/adminView/**", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/myPage/**").hasRole("BOYUG")
                        .requestMatchers("/personal/personalHome").hasRole("USER")
                        .anyRequest().permitAll()) // 그 외 모든 요청은 허용
                .sessionManagement(session -> session
                        // jwt: 세션 관리 상태 없음으로 구성 - 세션 생성 or 사용x
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                        .maximumSessions(1)) // 동시 세션 제어
//                .exceptionHandling(configurer -> configurer.accessDeniedHandler(accessDeniedHandler())) // 권한 부족시 404 페이지 session 인증일 때 쓰기
                .httpBasic(AbstractHttpConfigurer::disable)
//                    .formLogin(AbstractHttpConfigurer::disable);
                .formLogin((login) -> login
                        .loginPage("/userView/account/login")
                        .usernameParameter("userPhone")
                        .passwordParameter("userPw")
                        .successHandler(loginSuccessHandler)
                        .loginProcessingUrl("/userView/account/process-login")
                )

                //JwtAuthFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 미인증 사용자
                        .accessDeniedHandler(jwtAccessDeniedHandler) // 접근 권한 없는 사용자
                )

                .logout((logout) -> logout
                        .logoutUrl("/userView/account/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(true) // 로그아웃시 로그인 했던 모든 정보를 삭제
                        .deleteCookies("JSESSIONID")); // 톰캣이 만든 세션Id

        // OAuth2
        http
                .oauth2Login(OAuth2ClientConfigurer -> OAuth2ClientConfigurer
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                        .userService(customOAuth2UserService))
                        .defaultSuccessUrl("/personal/personalHome", true)
                        .failureHandler(customAuthenticationFailureHandler));
        // remember-me
//        http.rememberMe( rememberMe -> rememberMe
//                .key(rememberMeKey)
//                .tokenRepository(persistentTokenRepository)
//                .tokenValiditySeconds(60 * 60 * 24 * 14) // 토큰 유효기간 14일
//                .authenticationSuccessHandler(loginSuccessHandler)
//                .alwaysRemember(false)
//                .userDetailsService(userDetailsService)
//        );

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


}
