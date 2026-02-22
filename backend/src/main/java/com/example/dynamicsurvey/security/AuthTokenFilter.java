package com.example.dynamicsurvey.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * [教學說明] 身份驗證攔截器 (Auth Token Filter)
 * -----------------------------------------------------------------------------
 * 【設計意圖】
 * 這是一個「安檢口」。它攔截每一個進入後端的 HTTP 請求，
 * 檢查 Request Header 中是否帶有合法的 JWT。
 *
 * 【運作流程】
 * 1. 抓取 Header 中的 "Authorization" 內容。
 * 2. 如果發現 "Bearer <TOKEN>"，則調用 JwtUtils 驗證。
 * 3. 驗證成功後，從資料庫載入用戶詳細資料 (UserDetails)。
 * 4. 將身分資訊存入「Security 上下文」，讓後續的 Controller 知道是誰在發送請求。
 */
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Step 1: 從請求中提取 JWT
            String jwt = parseJwt(request);
            
            // Step 2: 驗證 Token 是否存在且合法
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Step 3: 從 Token 中解出用戶帳號 (Email)
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // Step 4: 根據帳號載入使用者詳細資訊與權限
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Step 5: 封裝成認證成功的 Token 物件
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                // Step 6: 附加請求細節 (如 IP、Session ID)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 7: 【關鍵】將認證結果登記到系統上下文中
                // 這樣後續的 Controller 呼叫 getPrincipal() 時才能拿到用戶資料
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("無法設定使用者認證資訊: {}", e.getMessage());
        }

        // 繼續執行後續的過濾器鏈 (安檢完畢，放行)
        filterChain.doFilter(request, response);
    }

    /**
     * [輔助工具] 解析 Header 中的 Bearer Token
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        // 檢查 Header 是否存在且格式為 "Bearer <Token>"
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            if (StringUtils.hasText(token)) {
                return token;
            }
        }

        return null;
    }
}
