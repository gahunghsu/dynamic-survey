package com.dynamic_survey.backend.controller;

import com.dynamic_survey.backend.dto.AuthenticationRequest;
import com.dynamic_survey.backend.dto.AuthenticationResponse;
import com.dynamic_survey.backend.dto.RegisterRequest;
import com.dynamic_survey.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [教學說明] AuthController (身份驗證控制器)
 * -----------------------------------------------------------------------------
 * 提供公開的 API 接口供前端呼叫。
 * 基於 SecurityConfig 的設定，所有 /api/auth/** 路徑都不需要 Token 即可存取。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService service;

    /**
     * [API] 使用者註冊
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    /**
     * [API] 使用者登入
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}
