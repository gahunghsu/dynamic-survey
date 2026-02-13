package com.dynamic_survey.backend.repository;

import com.dynamic_survey.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * [教學說明] UserRepository (使用者資料存取層)
 * -----------------------------------------------------------------------------
 * 1. @Repository: 標記此介面為 Spring Bean，負責資料庫操作。
 * 2. extends JpaRepository<User, Long>: 繼承 Spring Data JPA 提供的通用介面。
 *    - User: 此 Repository 管理的實體類別。
 *    - Long: 實體的主鍵 (Primary Key) 型態。
 * 
 * 繼承後，我們直接擁有 save(), findById(), findAll(), delete() 等方法，無需手寫 SQL。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * [教學說明] Derived Query Method (衍生查詢方法)
     * Spring Data JPA 的強大功能：透過方法名稱自動生成 SQL。
     * 
     * 方法名: findByEmail
     * 解析後: SELECT * FROM users WHERE email = ?
     * 
     * @param email 使用者輸入的 Email
     * @return Optional<User> 使用 Optional 包裝回傳值，避免 NullPointerException。
     */
    Optional<User> findByEmail(String email);
}
