package com.example.dynamicsurvey.security;

import com.example.dynamicsurvey.entity.User;
import com.example.dynamicsurvey.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [教學說明] 使用者詳情服務 (User Details Service)
 * -----------------------------------------------------------------------------
 * 【設計意圖】
 * 這是 Spring Security 要求的核心介面。它的唯一任務就是「根據帳號 (Email) 找到使用者」。
 * 它充當了資料庫 (JPA) 與 Security 認證機制之間的橋樑。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    UserRepository userRepository;

    /**
     * [功能] 載入使用者資料
     * @param email 使用者輸入的帳號
     * @return 封裝好的 UserDetails 物件
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. 從資料庫中搜尋使用者
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("找不到該帳號: " + email));

        // 2. 將找到的實體轉換為 Security 要求的格式並回傳
        return UserDetailsImpl.build(user);
    }
}
