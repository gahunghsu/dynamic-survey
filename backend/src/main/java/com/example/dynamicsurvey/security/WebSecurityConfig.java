package com.example.dynamicsurvey.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * [教學說明] 全域安全配置類別 (Web Security Configuration)
 * -----------------------------------------------------------------------------
 * 【設計意圖】
 * 這是系統安全的「大腦」，負責定義所有的安全規則。
 * 包含：誰可以存取哪個路徑、密碼要怎麼加密、如何處理跨域問題等。
 */
@Configuration
@EnableMethodSecurity // 開啟方法層級的權限控管 (如 @PreAuthorize)
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    /**
     * [組件] JWT 過濾器實例
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * [組件] 定義認證提供者：連結 UserDetailsService 與 PasswordEncoder
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder()); // 設定使用 BCrypt 加密
        return authProvider;
    }

    /**
     * [組件] 認證管理器：負責協調認證流程的核心物件
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * [組件] 密碼加密器
     * 選擇 BCrypt 強雜湊演算法，這在業界是標準做法。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * [核心] 安全過濾鏈配置 (Security Filter Chain)
     * -------------------------------------------------------------------------
     * 【教學重點】
     * 1. csrf.disable(): 因為使用 JWT，不需防禦 CSRF 攻擊。
     * 2. cors(): 開啟跨域支援，讓前端 Angular (4200 port) 能存取後端。
     * 3. sessionManagement: 設定為 STATELESS (無狀態)，不建立 HttpSession。
     * 4. authorizeHttpRequests: 定義路徑權限。
     *    - /api/auth/**: 公開
     *    - /api/admin/**: 僅限 ADMIN 角色
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // 停用 CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 設定 CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 無狀態架構
                .authorizeHttpRequests(auth -> 
                    auth.requestMatchers("/api/auth/**").permitAll() // 登入註冊開放
                        .requestMatchers("/api/surveys").permitAll()     // 問卷列表開放
                        .requestMatchers("/api/surveys/{id}/details").permitAll() // 問卷詳情開放
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // 後台限管理員
                        .anyRequest().authenticated() // 其餘請求皆需登入
                );

        // 設定認證提供者
        http.authenticationProvider(authenticationProvider());
        
        // 【重要】在處理使用者名稱密碼之前，先執行 JWT 過濾檢查
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * [功能] 跨域資源共享設定 (CORS)
     * 目的：允許前端 Angular (localhost:4200) 發送請求給後端。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200")); // 許可來源
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 許可方法
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // 許可標頭
        configuration.setAllowCredentials(true); // 許可攜帶 Cookie/憑證
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 應用到所有路徑
        return source;
    }
}
