package com.dynamic_survey.backend.dto;

import com.dynamic_survey.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [教學說明] UserResponse (使用者資訊回應 DTO)
 * -----------------------------------------------------------------------------
 * 用於回傳給前端的使用者基本資料。
 * 安全提醒：絕對不要在 Response 中包含密碼 (Password) 欄位。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private User.Role role;
}
