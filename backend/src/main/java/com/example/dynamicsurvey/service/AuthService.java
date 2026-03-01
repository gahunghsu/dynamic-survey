package com.example.dynamicsurvey.service;

import com.example.dynamicsurvey.dto.LoginRequest;
import com.example.dynamicsurvey.dto.RegisterRequest;
import com.example.dynamicsurvey.entity.User;
import com.example.dynamicsurvey.repository.UserRepository;
import com.example.dynamicsurvey.security.JwtUtils;
import com.example.dynamicsurvey.security.UserDetailsImpl;
import com.example.dynamicsurvey.vo.AppResponse;
import com.example.dynamicsurvey.vo.RspCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * [教學說明] 認證與授權業務邏輯層 (Auth Service)
 */
@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    /**
     * [功能] 使用者登入驗證
     */
    public AppResponse<?> authenticateUser(LoginRequest loginRequest) {
        // 1. 執行身分驗證
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // 2. 登記在案
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 3. 簽發 JWT
        String jwt = jwtUtils.generateJwtToken(authentication);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);

        return AppResponse.success(response);
    }

    /**
     * [功能] 註冊新帳號
     */
    public AppResponse<?> registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return AppResponse.error(RspCode.DUPLICATE_ERROR, "錯誤：此電子郵件已被使用！");
        }

        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setName(signUpRequest.getName());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setPhone(signUpRequest.getPhone());
        user.setRole("ADMIN"); // 預設註冊為管理員

        userRepository.save(user);
        
        // 修正：使用更標準的初始化方式
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(signUpRequest.getEmail());
        loginReq.setPassword(signUpRequest.getPassword());
        
        return authenticateUser(loginReq);
    }

    public AppResponse<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            return AppResponse.error(RspCode.UNAUTHORIZED);
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return AppResponse.error(RspCode.NOT_FOUND);
        
        return AppResponse.success(userToMap(user));
    }

    public AppResponse<?> updateProfile(Map<String, String> updates) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return AppResponse.error(RspCode.NOT_FOUND);

        if (updates.containsKey("name")) user.setName(updates.get("name"));
        if (updates.containsKey("phone")) user.setPhone(updates.get("phone"));
        if (updates.containsKey("password") && updates.get("password") != null && !updates.get("password").isEmpty()) {
            user.setPassword(encoder.encode(updates.get("password")));
        }

        userRepository.save(user);
        return AppResponse.success(userToMap(user));
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("name", user.getName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole());
        return userInfo;
    }
}
