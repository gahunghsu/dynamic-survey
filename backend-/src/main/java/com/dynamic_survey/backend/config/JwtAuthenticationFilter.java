package com.dynamic_survey.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * [教學說明] JwtAuthenticationFilter (JWT 認證過濾器)
 * -----------------------------------------------------------------------------
 * 這是 Spring Security 過濾器鏈 (Filter Chain) 中的關鍵一環。
 * 繼承 OncePerRequestFilter 確保每個 Request 只會被執行一次。
 *
 * 它的任務是：
 * 1. 攔截請求，檢查 Header 是否包含 "Authorization: Bearer <token>"。
 * 2. 如果有 Token，呼叫 JwtService 解析出 Username (Email)。
 * 3. 檢查目前 SecurityContext 是否已有認證資訊 (避免重複認證)。
 * 4. 若 Token 有效，將使用者資訊 (UserDetails) 存入 SecurityContextHolder。
 *    這樣後續的 Controller 就能透過 @AuthenticationPrincipal 取得當前登入者。
 */
@Component
@RequiredArgsConstructor // 自動生成包含 final 欄位的建構子 (DI)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 取得 Authorization Header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. 檢查 Header 格式：必須以 "Bearer " 開頭
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 不處理，直接交給下一個 Filter
            return;
        }

        // 3. 提取 Token (去除 "Bearer " 前綴)
        jwt = authHeader.substring(7);

        // 4. 解析 Username (若 Token 格式錯誤或過期，這裡可能會拋出異常)
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Token 解析失敗 (過期、格式錯誤等)，直接放行，讓後續 Filter 處理授權
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 若解析成功，且目前 Context 中尚未有認證資訊
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            try {
                // 從資料庫載入使用者詳情
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 驗證 Token 是否有效
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    
                    // 建立認證 Token 物件 (UsernamePasswordAuthenticationToken)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // 設定請求詳情
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // [重要] 將認證物件存入 SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // 如果找不到使用者或 Token 無效，不代表請求失敗 (可能是公開 API)
                // 我們不拋出異常，讓 filterChain 往下走，由 SecurityConfig 決定是否攔截
            }
        }
        
        // 繼續執行下一個 Filter
        filterChain.doFilter(request, response);
    }
}
