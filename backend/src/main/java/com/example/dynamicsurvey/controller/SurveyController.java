package com.example.dynamicsurvey.controller;

import com.example.dynamicsurvey.dto.ResponseDTO;
import com.example.dynamicsurvey.service.SurveyService;
import com.example.dynamicsurvey.vo.AppResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * [教學說明] 前台問卷流程控制器 (Public Survey Controller)
 * -----------------------------------------------------------------------------
 * 【設計意圖】
 * 處理一般使用者與遊客的請求，包含查看公開問卷與提交作答。
 */
@RestController
@RequestMapping("/api/surveys")
public class SurveyController {
    @Autowired
    SurveyService surveyService;

    /**
     * [功能] 取得當前可填寫的問卷列表
     */
    @GetMapping
    public AppResponse<?> getActiveSurveys() {
        return surveyService.getActiveSurveys();
    }

    /**
     * [功能] 取得問卷作答詳情
     * [技術細節] 明確指定 PathVariable 名稱為 "id"。
     */
    @GetMapping("/{id}/details")
    public AppResponse<?> getSurveyDetails(@PathVariable("id") Long id) {
        return surveyService.getSurveyDetails(id);
    }

    /**
     * [功能] 提交問卷答案
     * [技術細節] 使用 POST 接收作答資料，並指定路徑變數名稱。
     */
    @PostMapping("/{id}/submit")
    public AppResponse<?> submitResponse(@PathVariable("id") Long id, @RequestBody ResponseDTO submission) {
        return surveyService.submitResponse(id, submission);
    }

    /**
     * [功能] 取得當前登入使用者的作答紀錄歷史
     */
    @GetMapping("/history")
    public AppResponse<?> getUserHistory() {
        return surveyService.getUserHistory();
    }
}
