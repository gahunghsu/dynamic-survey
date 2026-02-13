package com.dynamic_survey.backend.service;

import com.dynamic_survey.backend.config.JwtService;
import com.dynamic_survey.backend.dto.AuthenticationRequest;
import com.dynamic_survey.backend.dto.AuthenticationResponse;
import com.dynamic_survey.backend.dto.RegisterRequest;
import com.dynamic_survey.backend.entity.User;
import com.dynamic_survey.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * [教學說明] AuthenticationService (認證邏輯服務)
 * -----------------------------------------------------------------------------
 * 處理與「帳號」相關的商業邏輯：
 * 1. 註冊 (Register): 建立新使用者並簽發 Token。
 * 2. 登入 (Login): 驗證帳密並簽發 Token。
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * [功能] 註冊新使用者
     * 1. 將明文密碼加密。
     * 2. 建立 User 實體並設定預設角色 (USER)。
     * 3. 儲存至資料庫。
     * 4. 生成並回傳 JWT Token。
     */
    public AuthenticationResponse register(RegisterRequest request) {
        // 檢查 Email 是否已被使用 (簡單範例，真實情況應拋出特定 Exception)
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 加密密碼！
                .phone(request.getPhone())
                .role(User.Role.USER) // 預設權限為一般使用者
                .build();

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * [功能] 登入驗證
     * 1. 使用 authenticationManager 進行驗證。
     *    這會自動呼叫 UserDetailsService 載入使用者，並透過 PasswordEncoder 比對密碼。
     * 2. 若驗證失敗，authenticate() 會拋出異常。
     * 3. 若成功，則查詢該使用者並生成 Token。
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 走到這代表認證成功
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(); // 理論上一定找得到

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
