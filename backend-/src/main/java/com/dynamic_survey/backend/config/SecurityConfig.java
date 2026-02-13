package com.dynamic_survey.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * [教學說明] SecurityConfig (Spring Security 主配置)
 * -----------------------------------------------------------------------------
 * 這是整個安全機制的總指揮中心。
 * 在 Spring Boot 3.0+ (Spring Security 6.0+) 中，語法有大幅更新，不再繼承 WebSecurityConfigurerAdapter。
 * 而是採用 Bean 風格的 SecurityFilterChain 配置。
 */
@Configuration
@EnableWebSecurity // 啟用 Web 安全機制
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. 關閉 CSRF (Cross-Site Request Forgery)
            // 因為我們使用 JWT (Stateless)，不需要 Session Cookie，所以 CSRF 攻擊風險較低，通常會關閉。
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. 請求授權規則 (Request Authorization)
            .authorizeHttpRequests(auth -> auth
                // 白名單：允許所有使用者存取 /api/auth/** 與公開問卷 API
                .requestMatchers("/api/auth/**", "/api/surveys/**").permitAll()
                // 其餘所有請求都需要認證 (Authenticated)
                .anyRequest().authenticated()
            )
            
            // 3. 設定 Session 管理策略
            // 因為是 RESTful API + JWT，所以設定為 STATELESS (無狀態)，不建立 HttpSession。
            .sessionManagement(sess -> sess
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 4. 設定 AuthenticationProvider
            .authenticationProvider(authenticationProvider)
            
            // 5. 新增 JWT Filter
            // 將我們的 jwtAuthFilter 插入到標準的 UsernamePasswordAuthenticationFilter 之前。
            // 這樣請求進來時，會先檢查 JWT，若有效則直接完成認證。
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
