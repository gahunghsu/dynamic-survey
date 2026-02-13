package com.dynamic_survey.backend.controller;

import com.dynamic_survey.backend.dto.UserResponse;
import com.dynamic_survey.backend.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [教學說明] UserController (使用者管理控制器)
 * -----------------------------------------------------------------------------
 * 此 Controller 中的所有 API 預設都是受保護的 (根據 SecurityConfig 設定)。
 * 呼叫這些 API 時，前端必須在 Header 帶上 "Authorization: Bearer <Token>"。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * [API] 取得當前登入者資料
     * GET /api/users/me
     * 
     * [教學重點] @AuthenticationPrincipal
     * Spring Security 會自動從 SecurityContext 中取出當前通過認證的 User 物件。
     * 我們不需要手動解析 Token，框架會幫我們注入到參數中。
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        // 將 Entity 轉換為 DTO 回傳
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
                
        return ResponseEntity.ok(response);
    }
}
