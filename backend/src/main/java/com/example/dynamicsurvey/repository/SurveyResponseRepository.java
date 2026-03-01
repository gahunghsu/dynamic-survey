package com.example.dynamicsurvey.repository;

import com.example.dynamicsurvey.entity.SurveyResponse;
import com.example.dynamicsurvey.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * [教學說明] 問卷回覆儲存庫
 */
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    List<SurveyResponse> findByUserOrderBySubmittedAtDesc(User user);
    
    // 用於統計，不需特定排序
    List<SurveyResponse> findBySurveyId(Long surveyId);
    
    // 【修正】用於列表顯示，依 ID 逆序排序 (最新在最前)
    List<SurveyResponse> findBySurveyIdOrderByIdDesc(Long surveyId);
    
    boolean existsBySurveyId(Long surveyId);
    boolean existsBySurveyIdAndEmail(Long surveyId, String email);
}
