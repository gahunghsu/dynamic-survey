package com.example.dynamicsurvey.controller;

import com.example.dynamicsurvey.dto.SurveyDTO;
import com.example.dynamicsurvey.service.SurveyService;
import com.example.dynamicsurvey.vo.AppResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * [教學說明] 後台問卷管理控制器 (Admin Survey Controller)
 */
@RestController
@RequestMapping("/api/admin/surveys")
public class AdminSurveyController {

    @Autowired
    SurveyService surveyService;

    @GetMapping
    public AppResponse<?> getSurveys(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false, name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return surveyService.getSurveysByAdmin(title, startDate, endDate);
    }

    @GetMapping("/{id}")
    public AppResponse<?> getSurveyById(@PathVariable("id") Long id) {
        return surveyService.getSurveyDetails(id);
    }

    /**
     * [功能] 建立新問卷 (直接儲存)
     */
    @PostMapping("")
    public AppResponse<?> createSurvey(@Valid @RequestBody SurveyDTO surveyDTO) {
        return surveyService.saveSurvey(surveyDTO);
    }

    /**
     * [功能] 1. 編輯問卷暫存至 Session
     */
    @PostMapping("/session-store")
    public AppResponse<?> storeSurveyInSession(@RequestBody SurveyDTO surveyDTO, HttpSession session) {
        return surveyService.saveAdminSurveyToSession(surveyDTO, session);
    }

    /**
     * [功能] 2. 從 Session 取得編輯中的問卷
     */
    @GetMapping("/session-get")
    public AppResponse<?> getSurveyFromSession(HttpSession session) {
        return surveyService.getAdminSurveyFromSession(session);
    }

    /**
     * [功能] 3. 確認提交問卷並決定是否發佈
     */
    @PostMapping("/confirm-commit")
    public AppResponse<?> confirmSurveyCommit(@RequestParam(name = "isPublish") boolean isPublish, HttpSession session) {
        return surveyService.commitAdminSurveyFromSession(isPublish, session);
    }

    @PutMapping("/{id}")
    public AppResponse<?> updateSurvey(@PathVariable("id") Long id, @Valid @RequestBody SurveyDTO surveyDTO) {
        surveyDTO.setId(id);
        return surveyService.saveSurvey(surveyDTO);
    }

    @DeleteMapping("/{id}")
    public AppResponse<?> deleteSurvey(@PathVariable("id") Long id) {
        return surveyService.deleteSurvey(id);
    }

    @GetMapping("/{id}/stats")
    public AppResponse<?> getSurveyStats(@PathVariable("id") Long id) {
        return surveyService.getSurveyStats(id);
    }

    @GetMapping("/{id}/responses")
    public AppResponse<?> getSurveyResponses(@PathVariable("id") Long id) {
        return surveyService.getSurveyResponses(id);
    }

    @GetMapping("/response-detail/{responseId}")
    public AppResponse<?> getResponseDetail(@PathVariable("responseId") Long responseId) {
        return surveyService.getResponseDetail(responseId);
    }
}
