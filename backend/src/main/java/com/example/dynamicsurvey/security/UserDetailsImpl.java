package com.example.dynamicsurvey.security;

import com.example.dynamicsurvey.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * [教學說明] 安全使用者詳情實作 (User Details Implementation)
 * -----------------------------------------------------------------------------
 * 【設計意圖】
 * Spring Security 並不直接認識我們的 `User` 實體類別。
 * 為了讓 Security 能處理認證，我們必須建立一個實作 `UserDetails` 介面的類別，
 * 作為我們自定義 User 與 Spring Security 之間的「適配器 (Adapter)」。
 */
@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private Long id;
    private String email;
    private String name;
    
    @JsonIgnore // 敏感資料：序列化為 JSON 時忽略此欄位
    private String password;

    // 存放該使用者的權限清單 (如 ROLE_ADMIN, ROLE_USER)
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * [靜態工廠] 將資料庫的 User 實體轉換為 Security 認識的 UserDetailsImpl
     */
    public static UserDetailsImpl build(User user) {
        // 將字串角色 (如 ADMIN) 轉換為 Security 要求的 GrantedAuthority 物件
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                authorities
        );
    }

    @Override
    public String getUsername() { return email; } // 我們使用 Email 作為登入帳號

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
