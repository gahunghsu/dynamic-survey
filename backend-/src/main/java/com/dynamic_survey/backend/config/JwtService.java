package com.dynamic_survey.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * [教學說明] JwtService (JWT 服務)
 * -----------------------------------------------------------------------------
 * 負責處理所有與 JWT (JSON Web Token) 相關的操作，包含：
 * 1. 生成 Token (Generate): 當使用者登入成功後，簽發一個 Token 給前端。
 * 2. 解析 Token (Extract): 從 Token 中取出使用者資訊 (Username/Email)。
 * 3. 驗證 Token (Validate): 檢查 Token 是否過期、簽名是否正確、使用者是否吻合。
 *
 * 核心概念：
 * - Claim: Token 內容的片段資訊 (如 subject=user_email, expiration=時間)。
 * - Signing Key: 用於加密簽署的密鑰，必須妥善保管，不能洩漏給前端。
 */
@Service
public class JwtService {

    /**
     * [教學說明] SECRET_KEY
     * 這是加密簽章用的密鑰。在真實專案中，應該放在 application.properties 或環境變數中。
     * 這裡為了教學方便，使用一個 Hard-code 的 Base64 編碼字串 (長度需足夠，至少 256 bits)。
     */
    // 這裡使用隨機生成的 256-bit Key
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    /**
     * [功能] 從 Token 中取得使用者名稱 (Email)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * [功能] 泛型方法：從 Token 中取得任意 Claim
     * @param claimsResolver 一個 Function，定義要取出哪個欄位
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * [功能] 生成 Token (不帶額外 Claims)
     * @param userDetails 使用者詳情 (來自 Spring Security)
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * [功能] 生成 Token (帶額外 Claims)
     * @param extraClaims 額外資訊 (如 role, userId 等)
     * @param userDetails 使用者詳情
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // 將 Email 設為 Subject
                .setIssuedAt(new Date(System.currentTimeMillis())) // 發行時間
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 過期時間：24小時
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // 使用 HS256 演算法簽名
                .compact();
    }

    /**
     * [功能] 驗證 Token 是否有效
     * 1. 取出的 username 是否與 UserDetails 中的 username 一致？
     * 2. Token 是否過期？
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * [功能] 解析 Token 的所有內容
     * 使用 Jwts parserBuilder 配合 SigningKey 來驗證簽名並解析內容。
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * [功能] 取得簽名密鑰物件
     * 將 Base64 字串解碼並轉換為 Key 物件。
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
