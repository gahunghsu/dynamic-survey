package com.example.dynamicsurvey.vo;

import lombok.Data;

/**
 * [教學說明] 統一回應物件 (Unified Response Wrapper)
 * -----------------------------------------------------------------------------
 * 目的：確保後端回傳給前端的 JSON 結構始終保持一致，方便前端進行統一攔截與解析。
 * 結構：
 *   - code: 業務狀態碼 (對應 RspCode)
 *   - message: 狀態說明
 *   - data: 實際的資料內容 (使用泛型 T 支援各種物件)
 */
@Data
public class AppResponse<T> {
    private int code;           // 狀態碼
    private String message;     // 訊息說明
    private T data;             // 資料內容 (可能是單一物件、列表或 null)

    /**
     * 建構子：僅包含狀態碼訊息 (適用於失敗或無回傳資料的操作)
     */
    public AppResponse(RspCode rspCode) {
        this.code = rspCode.getCode();
        this.message = rspCode.getMessage();
    }

    /**
     * 建構子：包含狀態碼與實際資料內容
     */
    public AppResponse(RspCode rspCode, T data) {
        this.code = rspCode.getCode();
        this.message = rspCode.getMessage();
        this.data = data;
    }
    
    /**
     * 靜態工廠方法：快速建立成功回應
     * @param data 要回傳給前端的資料
     */
    public static <T> AppResponse<T> success(T data) {
        return new AppResponse<>(RspCode.SUCCESS, data);
    }
    
    /**
     * 靜態工廠方法：快速建立錯誤回應 (帶有預設訊息)
     */
    public static <T> AppResponse<T> error(RspCode rspCode) {
        return new AppResponse<>(rspCode);
    }

    /**
     * 靜態工廠方法：自定義錯誤訊息 (例如顯示具體的驗證錯誤)
     */
    public static <T> AppResponse<T> error(RspCode rspCode, String customMessage) {
        AppResponse<T> response = new AppResponse<>(rspCode);
        response.setMessage(customMessage); // 覆蓋 RspCode 的預設訊息
        return response;
    }
}
