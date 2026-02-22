package com.example.dynamicsurvey.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * [教學說明] JWT 工具類別 (JWT Utilities)
 * -----------------------------------------------------------------------------
 * 【設計意圖】
 * 本類別封裝了所有關於 JSON Web Token 的邏輯：產生、解析與驗證。
 * JWT 是無狀態認證的核心，讓我們不需在伺服器存 Session 也能辨識用戶。
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}") // 從 application.properties 讀取密鑰
    private String jwtSecret;

    @Value("${jwt.expiration}") // 從 application.properties 讀取過期時間 (毫秒)
    private int jwtExpirationMs;

    /**
     * [工具] 產生簽名所需的金鑰 (Key)
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * [功能] 產生 JWT Token
     * 呼叫時機：使用者成功登入後。
     */
    public String generateJwtToken(Authentication authentication) {
        // 獲取目前通過驗證的使用者主體
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        // 建立 JWT
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // 設定主題 (即 Email)
                .setIssuedAt(new Date())                // 設定簽發時間
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // 設定過期時間
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // 使用 HS512 演算法與密鑰簽名
                .compact(); // 壓縮成最終的 Base64 字串
    }

    /**
     * [功能] 從 Token 中提取使用者帳號 (Email)
     * 呼叫時機：驗證 Token 成功後，需要知道這個 Token 是誰的。
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * [功能] 驗證 JWT Token 的合法性
     * 呼叫時機：攔截器攔截到請求時。
     * 檢查點：格式是否正確、是否被竄改過、是否已過期。
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
            logger.error("無效的 JWT 簽名: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("無效的 JWT 格式: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT 已過期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("不支援的 JWT 類型: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT 聲明字串為空: {}", e.getMessage());
        }
        return false;
    }
}
