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
 * -----------------------------------------------------------------------------
 * 【設計意圖】
 * 本服務負責處理所有與「身分」相關的邏輯，包含使用者的註冊、登入驗證、以及資料修改。
 * 它是系統安全性的核心，確保只有合法的用戶能取得憑證 (JWT)。
 */
@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager; // Spring Security 核心驗證器

    @Autowired
    UserRepository userRepository; // 使用者資料存取介面

    @Autowired
    PasswordEncoder encoder; // 密碼加密器 (實作為 BCrypt)

    @Autowired
    JwtUtils jwtUtils; // JWT 工具類別

    /**
     * [功能] 使用者登入驗證
     * -------------------------------------------------------------------------
     * 【教學重點：驗證流程】
     * 1. 封裝 Token：將帳密封裝進 UsernamePasswordAuthenticationToken。
     * 2. 執行驗證：authenticationManager 會自動比對資料庫與加密後的密碼。
     * 3. 簽發憑證：驗證成功後，使用 JwtUtils 產生一個代表身分的字串 (JWT)。
     */
    /**
     * [功能] 使用者登入驗證 (Authentication Flow)
     * -------------------------------------------------------------------------
     * 【深度教學：Spring Security 核心組件解析】
     * 
     * 1. UsernamePasswordAuthenticationToken:
     *    - 這是身分資料的「載體」。
     *    - 此時傳入的是「未經驗證」的 Token，裡面只裝著使用者輸入的 email 與 password。
     * 
     * 2. AuthenticationManager:
     *    - 這是系統的「認證管理器/裁判」。
     *    - 它會調用 .authenticate() 方法開始進行比對。
     *    - 內部會自動尋找 UserDetailsService (我們寫的 UserDetailsServiceImpl) 來抓取資料庫真實密碼，
     *      並使用 PasswordEncoder (BCrypt) 進行雜湊比對。
     * 
     * 3. .authenticate() 的結果：
     *    - 若密碼錯誤：會直接拋出「BadCredentialsException」，流程中斷。
     *    - 若驗證成功：會回傳一個全新的 Authentication 物件，裡面裝滿了使用者的詳細資料與權限 (Roles)。
     * 
     * 4. SecurityContextHolder.getContext().setAuthentication(authentication):
     *    - 這是將驗證成功的「勳章」掛在目前的執行緒 (Thread) 上。
     *    - 讓 Spring 在後續的 Request 流程中，知道「這個人是誰、有哪些權限」。
     * 
     * 5. JWT (Json Web Token):
     *    - 因為我們採用「無狀態 (Stateless)」架構，伺服器不存 Session。
     *    - 因此必須產生一個「數位憑證 (JWT)」交給前端。
     *    - 前端後續的每個請求都要帶上這個憑證，伺服器才會認得他。
     */
    public AppResponse<?> authenticateUser(LoginRequest loginRequest) {
        // Step A: 將前端傳來的帳密，包裝成 Spring Security 規定的格式
        UsernamePasswordAuthenticationToken unauthenticatedToken = 
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        // Step B: 把這包資料丟給「裁判」去驗證。
        // 這行代碼會自動去資料庫找人、比對密碼、檢查帳號是否被鎖定。
        Authentication authentication = authenticationManager.authenticate(unauthenticatedToken);

        // Step C: 驗證成功後，把「認證通過」的身分資訊存在 Security 上下文中。
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Step D: 利用驗證成功的結果 (包含使用者 ID, 姓名, 權限)，簽發一個 JWT 字串。
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Step E: 封裝成統一格式回傳給前端。
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);

        return AppResponse.success(response);
    }

    /**
     * [功能] 註冊新帳號
     * -------------------------------------------------------------------------
     * 【設計亮點：安全性與使用者體驗】
     * 1. 唯一性檢查：避免電子郵件重複註冊。
     * 2. 密碼雜湊：【重要】絕對不能存明文！使用 BCrypt 演算法進行不可逆加密。
     * 3. 權限賦予：預設給予角色 (如 ADMIN 或 USER)。
     * 4. 無感登入：註冊成功後直接執行登入邏輯，讓使用者不需再次輸入帳密。
     */
    public AppResponse<?> registerUser(RegisterRequest signUpRequest) {
        // Step 1: 檢查電子郵件是否已被註冊
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return AppResponse.error(RspCode.DUPLICATE_ERROR, "錯誤：此電子郵件已被使用！");
        }

        // Step 2: 建立新的 User 實體並填充資料
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setName(signUpRequest.getName());
        
        // 【核心教學點】使用 encoder.encode 進行密碼加密
        // BCrypt 會加入「隨機鹽值 (Salt)」，即使兩個人密碼一樣，加密後的字串也會不同。
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        
        user.setPhone(signUpRequest.getPhone());
        
        // 開發測試階段，預設註冊為管理員以方便測試所有功能
        user.setRole("ADMIN"); 

        // Step 3: 寫入資料庫
        userRepository.save(user);
        
        // Step 4: [優化體驗] 註冊成功後直接調用登入方法，回傳 Token 給前端
        return authenticateUser(new LoginRequest() {{
            setEmail(signUpRequest.getEmail());
            setPassword(signUpRequest.getPassword());
        }});
    }

    /**
     * [功能] 取得當前登入的使用者詳細資料
     */
    public AppResponse<?> getCurrentUser() {
        // 從目前的 SecurityContext 中提取已驗證的身分資訊
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return AppResponse.error(RspCode.NOT_FOUND);
        
        return AppResponse.success(userToMap(user));
    }

    /**
     * [功能] 修改會員資料 (個人檔案更新)
     * -------------------------------------------------------------------------
     * 【技術細節】
     * 1. 只更新有傳入的欄位 (如 name, phone)。
     * 2. 若有傳入新密碼，則重新進行 BCrypt 加密後儲存。
     */
    public AppResponse<?> updateProfile(Map<String, String> updates) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return AppResponse.error(RspCode.NOT_FOUND);

        // 動態判斷並更新資料
        if (updates.containsKey("name")) user.setName(updates.get("name"));
        if (updates.containsKey("phone")) user.setPhone(updates.get("phone"));
        
        // 若要修改密碼，必須經過加密流程
        if (updates.containsKey("password") && updates.get("password") != null && !updates.get("password").isEmpty()) {
            user.setPassword(encoder.encode(updates.get("password")));
        }

        userRepository.save(user);
        return AppResponse.success(userToMap(user));
    }

    /**
     * [輔助方法] 將 User 實體轉換為前端需要的 Map 格式 (避免洩漏密碼欄位)
     */
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
