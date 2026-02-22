package com.example.dynamicsurvey.controller;

import com.example.dynamicsurvey.dto.LoginRequest;
import com.example.dynamicsurvey.dto.RegisterRequest;
import com.example.dynamicsurvey.service.AuthService;
import com.example.dynamicsurvey.vo.AppResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public AppResponse<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

    @PostMapping("/register")
    public AppResponse<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.registerUser(registerRequest);
    }
}
