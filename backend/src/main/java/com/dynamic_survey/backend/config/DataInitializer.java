package com.dynamic_survey.backend.config;

import com.dynamic_survey.backend.entity.User;
import com.dynamic_survey.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * [教學說明] DataInitializer (資料初始化器)
 * -----------------------------------------------------------------------------
 * 實作 CommandLineRunner 介面，讓程式在啟動後自動執行指定邏輯。
 * 我們在這裡檢查資料庫是否已存在管理員，若無則自動建立一個預設帳號。
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. 檢查是否已存在管理員 (admin@example.com)
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            
            // 2. 建立預設管理員
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123")) // 預設密碼
                    .phone("0912345678")
                    .role(User.Role.ADMIN) // 設定為管理員權限
                    .build();

            userRepository.save(admin);
            
            System.out.println("--------------------------------------------------");
            System.out.println("預設管理員帳號已建立！");
            System.out.println("帳號: admin@example.com");
            System.out.println("密碼: admin123");
            System.out.println("--------------------------------------------------");
        }
    }
}
