package com.dynamic_survey.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * [教學說明] User Entity (使用者實體)
 * -----------------------------------------------------------------------------
 * 1. @Entity: 標記此類別為 JPA 實體，會對應到資料庫中的一張表。
 * 2. @Table(name = "users"): 指定資料庫中的表名為 "users"。
 * 3. implements UserDetails: 實作 Spring Security 的核心介面，讓 Security 機制能理解此使用者物件。
 * 4. Lombok 註解 (@Data, @Builder...): 自動產生 Getter/Setter/Constructor，簡化程式碼。
 */
@Data // 自動產生 Getter, Setter, toString, equals, hashCode
@Builder // 提供 Builder Pattern (User.builder().email(...).build())
@NoArgsConstructor // 無參數建構子 (JPA 必須)
@AllArgsConstructor // 全參數建構子 (Builder 需要)
@Entity
@Table(name = "users")
public class User implements UserDetails {

    /**
     * [教學說明] @Id 與 @GeneratedValue
     * 指定 id 為 Primary Key (主鍵)。
     * GenerationType.IDENTITY 代表使用資料庫的 Auto Increment (自動遞增) 機制。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * [教學說明] @Column(unique = true, nullable = false)
     * email 欄位必須唯一且不能為空。這是登入時的識別帳號。
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * [教學說明] 密碼欄位
     * 注意：這裡儲存的必須是經過 BCrypt 加密後的雜湊值 (Hash)，絕不能存明碼。
     */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String phone;

    /**
     * [教學說明] @Enumerated(EnumType.STRING)
     * 將 Enum 型態以字串形式存入資料庫 (例如存 "ADMIN" 而不是 0 或 1)。
     * 這樣資料庫的可讀性較高。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * [教學說明] 定義角色列舉
     * 系統中有兩種角色：一般使用者 (USER) 與 管理員 (ADMIN)。
     */
    public enum Role {
        USER,
        ADMIN
    }

    // -------------------------------------------------------------------------
    // UserDetails 介面實作方法 (Spring Security 用)
    // -------------------------------------------------------------------------

    /**
     * [教學說明] getAuthorities
     * 回傳使用者的權限集合。Spring Security 會根據此方法判斷使用者能存取哪些 API。
     * 我們將 Role 轉換為 SimpleGrantedAuthority。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * [教學說明] getUsername
     * Spring Security 用來識別使用者的欄位。在本系統中，我們使用 email 作為帳號。
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * [教學說明] 帳號狀態檢查
     * 以下四個方法用於判斷帳號是否過期、鎖定或停用。
     * 為了教學簡化，我們全部回傳 true (代表帳號正常可用)。
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
