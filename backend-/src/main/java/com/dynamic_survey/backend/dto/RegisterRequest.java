package com.dynamic_survey.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [教學說明] RegisterRequest (註冊請求 DTO)
 * -----------------------------------------------------------------------------
 * DTO (Data Transfer Object) 用於封裝前端傳來的資料。
 * 為什麼不直接用 Entity？
 * 1. 安全性：Entity 包含敏感欄位 (如 id, role)，不應由前端直接控制。
 * 2. 彈性：前端需要的欄位可能與資料庫結構不同。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
}
