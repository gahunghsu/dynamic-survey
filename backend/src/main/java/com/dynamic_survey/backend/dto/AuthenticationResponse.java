package com.dynamic_survey.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [教學說明] AuthenticationResponse (認證回應 DTO)
 * -----------------------------------------------------------------------------
 * 登入或註冊成功後，回傳給前端的 Token 資訊。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
}
