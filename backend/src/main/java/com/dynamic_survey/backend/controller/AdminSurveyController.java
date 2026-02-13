package com.dynamic_survey.backend.controller;

import com.dynamic_survey.backend.dto.SurveyDTO;
import com.dynamic_survey.backend.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [教學說明] AdminSurveyController (管理員問卷控制器)
 * -----------------------------------------------------------------------------
 * 專供管理員使用的問卷管理 API。
 */
@RestController
@RequestMapping("/api/admin/surveys")
@RequiredArgsConstructor
public class AdminSurveyController {

    private final SurveyService surveyService;

    /**
     * [API] 取得所有問卷列表
     */
    @GetMapping
    public ResponseEntity<List<SurveyDTO>> getAllSurveys() {
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    /**
     * [API] 取得單一問卷詳情
     */
    @GetMapping("/{id}")
    public ResponseEntity<SurveyDTO> getSurveyById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(surveyService.getSurveyById(id));
    }

    /**
     * [API] 取得問卷統計數據
     * GET /api/admin/surveys/{id}/stats
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<com.dynamic_survey.backend.dto.SurveyStatsDTO> getSurveyStats(@PathVariable("id") Long id) {
        return ResponseEntity.ok(surveyService.getSurveyStats(id));
    }

    /**
     * [API] 建立新問卷
     */
    @PostMapping
    public ResponseEntity<SurveyDTO> createSurvey(@RequestBody SurveyDTO surveyDTO) {
        return ResponseEntity.ok(surveyService.saveSurvey(surveyDTO));
    }

    /**
     * [API] 更新問卷
     */
    @PutMapping("/{id}")
    public ResponseEntity<SurveyDTO> updateSurvey(
            @PathVariable("id") Long id, 
            @RequestBody SurveyDTO surveyDTO
    ) {
        surveyDTO.setId(id);
        return ResponseEntity.ok(surveyService.saveSurvey(surveyDTO));
    }

    /**
     * [API] 刪除問卷
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable("id") Long id) {
        surveyService.deleteSurvey(id);
        return ResponseEntity.noContent().build();
    }
}
