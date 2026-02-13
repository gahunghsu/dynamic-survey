package com.dynamic_survey.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dynamic_survey.backend.dto.SurveyDTO;
import com.dynamic_survey.backend.service.SurveyService;

import lombok.RequiredArgsConstructor;

/**
 * [教學說明] PublicSurveyController (前台問卷控制器)
 * -----------------------------------------------------------------------------
 * 供一般使用者使用的 API，主要負責獲取可填寫的問卷。
 * 此控制器對應的路徑應在 SecurityConfig 中設為 permitAll()。
 */
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class PublicSurveyController {

    private final SurveyService surveyService;

    /**
     * [API] 取得目前所有可填寫的問卷列表
     */
    @GetMapping
    public ResponseEntity<List<SurveyDTO>> getActiveSurveys() {
        return ResponseEntity.ok(surveyService.getActiveSurveys());
    }

    /**
     * [API] 取得問卷詳情 (填寫用)
     */
    @GetMapping("/{id}")
    public ResponseEntity<SurveyDTO> getSurveyById(@PathVariable Long id) {
        // 實務上這裡應多檢查該 ID 是否真的處於 PUBLISHED 且在期限內
        return ResponseEntity.ok(surveyService.getSurveyById(id));
    }

    /**
     * [API] 提交問卷答案
     * POST /api/surveys/{id}/submit
     * 需要登入才能提交，使用 @org.springframework.security.core.annotation.AuthenticationPrincipal 取得當前使用者。
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submitResponse(
            @PathVariable Long id,
            @RequestBody com.dynamic_survey.backend.dto.ResponseDTO responseDTO,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.dynamic_survey.backend.entity.User user
    ) {
        responseDTO.setSurveyId(id);
        surveyService.submitResponse(responseDTO, user);
        return ResponseEntity.ok().build();
    }

    /**
     * [API] 取得個人的填寫紀錄
     * GET /api/surveys/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<com.dynamic_survey.backend.dto.ResponseHistoryDTO>> getMyHistory(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.dynamic_survey.backend.entity.User user
    ) {
        return ResponseEntity.ok(surveyService.getUserHistory(user.getId()));
    }
}
