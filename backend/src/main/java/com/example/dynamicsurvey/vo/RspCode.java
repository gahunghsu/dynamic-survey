package com.example.dynamicsurvey.vo;

import lombok.Getter;

/**
 * [教學說明] 狀態碼列舉 (Response Code Enumeration)
 * -----------------------------------------------------------------------------
 * 目的：集中管理系統中所有的回應代碼與訊息，避免在程式碼中出現「魔術數字」(Magic Numbers)。
 * 優點：提高程式碼可讀性，且未來修改訊息時只需變動此處。
 */
@Getter
public enum RspCode {
    // === 定義狀態常數 (代碼, 訊息) ===
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "參數錯誤"),
    UNAUTHORIZED(401, "尚未登入或憑證無效"),
    FORBIDDEN(403, "權限不足"),
    NOT_FOUND(404, "資源不存在"),
    DUPLICATE_ERROR(409, "資料重複"),
    INTERNAL_SERVER_ERROR(500, "系統內部錯誤");

    // 存放對應的 HTTP 狀態碼
    private final int code;
    
    // 存放給前端顯示的說明訊息
    private final String message;

    /**
     * 建構子：用於建立列舉實例並賦值
     */
    RspCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
