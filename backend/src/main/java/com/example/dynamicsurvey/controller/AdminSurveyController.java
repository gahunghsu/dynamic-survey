package com.example.dynamicsurvey.controller;

import com.example.dynamicsurvey.dto.SurveyDTO;
import com.example.dynamicsurvey.service.SurveyService;
import com.example.dynamicsurvey.vo.AppResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * [教學說明] 後台問卷管理控制器 (Admin Survey Controller)
 * -----------------------------------------------------------------------------
 * 【設計意圖】
 * 本類別負責處理管理員 (ADMIN) 的各項操作請求。它作為系統的「守門員」，
 * 負責接收 HTTP 請求、驗證權限與資料合法性，並將請求分發給 Service 層處理。
 *
 * 【關鍵技術】
 * 1. @RestController: 代表這是一個 REST 風格的控制器，回傳值會自動轉換為 JSON。
 * 2. @RequestMapping: 定義此控制器下所有 API 的基礎路徑為 "/api/admin/surveys"。
 * 3. 權限控管: 配合 SecurityConfig，此路徑僅限具有 ROLE_ADMIN 權限的用戶存取。
 */
@RestController
@RequestMapping("/api/admin/surveys")
public class AdminSurveyController {

    @Autowired
    SurveyService surveyService;

    /**
     * [功能] 查詢問卷列表 (支援動態篩選)
     * -------------------------------------------------------------------------
     * 【技術細節】
     * 1. @RequestParam: 接收 URL Query String 參數。
     * 2. [修正] name = "xxx": 在 Spring Boot 3.2+ 中必須明確指定參數名稱，否則會噴 IllegalArgumentException。
     * 3. @DateTimeFormat: 強制將前端傳來的字串轉換為指定的 LocalDate 格式。
     */
    @GetMapping
    public AppResponse<?> getSurveys(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false, name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return surveyService.getSurveysByAdmin(title, startDate, endDate);
    }

    /**
     * [功能] 取得單一問卷詳情 (編輯前置作業)
     * -------------------------------------------------------------------------
     * 【技術細節】
     * 1. @PathVariable("id"): 從 URL 路徑中提取動態變數，並明確指定名稱為 "id"。
     */
    @GetMapping("/{id}")
    public AppResponse<?> getSurveyById(@PathVariable("id") Long id) {
        return surveyService.getSurveyDetails(id);
    }

    /**
     * [功能] 建立新問卷
     * -------------------------------------------------------------------------
     * 【教學重點】資料驗證 (Bean Validation)
     * 1. @PostMapping: 指定使用 POST 方法，用於「新增」資源。
     * 2. @Valid: 這是核心註解！它會觸發 SurveyDTO 中定義的驗證規則 (如 @NotBlank)。
     *    若驗證失敗，會由 GlobalExceptionHandler 攔截並回傳錯誤訊息。
     * 3. @RequestBody: 指示 Spring 將請求體中的 JSON 自動映射成 Java 對象。
     */
    @PostMapping
    public AppResponse<?> createSurvey(@Valid @RequestBody SurveyDTO surveyDTO) {
        return surveyService.saveSurvey(surveyDTO);
    }

    /**
     * [功能] 修改現有問卷 (包含題目與選項)
     * -------------------------------------------------------------------------
     * 【技術細節】
     * 1. @PutMapping: 指定使用 PUT 方法，用於「完整更新」資源。
     * 2. 安全性做法：透過 surveyDTO.setId(id) 確保更新的是路徑中指定的該筆資料。
     */
    @PutMapping("/{id}")
    public AppResponse<?> updateSurvey(@PathVariable("id") Long id, @Valid @RequestBody SurveyDTO surveyDTO) {
        surveyDTO.setId(id);
        return surveyService.saveSurvey(surveyDTO);
    }

    /**
     * [功能] 刪除問卷
     * -------------------------------------------------------------------------
     * 【技術細節】
     * 1. @DeleteMapping: 指定使用 DELETE 方法。
     * 2. 業務防呆：Service 會在內部檢查該問卷是否已有作答紀錄。
     */
    @DeleteMapping("/{id}")
    public AppResponse<?> deleteSurvey(@PathVariable("id") Long id) {
        return surveyService.deleteSurvey(id);
    }

    /**
     * [功能] 取得問卷統計分析數據 (圓餅圖與文字回答)
     */
    @GetMapping("/{id}/stats")
    public AppResponse<?> getSurveyStats(@PathVariable("id") Long id) {
        return surveyService.getSurveyStats(id);
    }

    /**
     * [功能] 取得該問卷的所有填寫者清單
     */
    @GetMapping("/{id}/responses")
    public AppResponse<?> getSurveyResponses(@PathVariable("id") Long id) {
        return surveyService.getSurveyResponses(id);
    }
}
