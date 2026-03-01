package com.example.dynamicsurvey.controller;

import com.example.dynamicsurvey.dto.ResponseDTO;
import com.example.dynamicsurvey.service.SurveyService;
import com.example.dynamicsurvey.vo.AppResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * [教學說明] 前台問卷流程控制器 (Public Survey Controller)
 * -----------------------------------------------------------------------------
 * 處理公開問卷列表、問卷詳情、以及包含 Session 確認頁的提交流程。
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
     * [修正] 明確指定 PathVariable("id")
     */
    @GetMapping("/{id}/details")
    public AppResponse<?> getSurveyDetails(@PathVariable("id") Long id) {
        return surveyService.getSurveyDetails(id);
    }

    /**
     * [功能] 1. 暫存作答資料至 Session (進入確認頁前呼叫)
     */
    @PostMapping("/session-store")
    public AppResponse<?> storeInSession(@RequestBody ResponseDTO submission, HttpSession session) {
        return surveyService.saveToSession(submission, session);
    }

    /**
     * [功能] 2. 從 Session 取得暫存資料 (確認頁唯讀顯示)
     */
    @GetMapping("/session-get")
    public AppResponse<?> getFromSession(HttpSession session) {
        return surveyService.getFromSession(session);
    }

    /**
     * [功能] 3. 正式提交問卷 (從 Session 轉存資料庫)
     */
    @PostMapping("/confirm")
    public AppResponse<?> confirmSubmit(HttpSession session) {
        return surveyService.commitFromSession(session);
    }

    /**
     * [功能] 直接提交 API (不經由 Session)
     * [修正] 明確指定 PathVariable("id")
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
