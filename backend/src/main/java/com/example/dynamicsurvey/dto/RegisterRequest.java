package com.example.dynamicsurvey.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "姓名不可為空")
    private String name;

    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "電子郵件格式不正確")
    private String email;

    @NotBlank(message = "密碼不可為空")
    @Size(min = 6, message = "密碼長度需至少 6 位")
    private String password;

    private String phone;
}
