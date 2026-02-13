package com.dynamic_survey.backend.config;

import com.dynamic_survey.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * [教學說明] ApplicationConfig (應用程式配置)
 * -----------------------------------------------------------------------------
 * 這個設定檔主要負責定義與「身份驗證邏輯」相關的 Bean。
 * 雖然 Spring Security 有預設配置，但我們需要連結自己的資料庫 (UserRepository)。
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * [教學說明] UserDetailsService
     * 這是 Spring Security 載入使用者資料的核心介面。
     * 我們使用 Lambda 表達式實作 loadUserByUsername 方法：
     * 透過 userRepository.findByEmail 查詢，若找不到則拋出異常。
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * [教學說明] AuthenticationProvider
     * 負責實際執行認證判斷的元件。我們使用 DaoAuthenticationProvider：
     * 1. 指定 UserDetailsService (去哪裡找人)
     * 2. 指定 PasswordEncoder (密碼怎麼比對)
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * [教學說明] AuthenticationManager
     * 這是認證機制的入口，Controller 在登入時會呼叫它來觸發認證流程。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * [教學說明] PasswordEncoder
     * 密碼加密器。Spring Security 推薦使用 BCrypt。
     * 它會自動處理 Salt (鹽值) 與 Hashing，確保相同的密碼每次加密結果都不同，增強安全性。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
