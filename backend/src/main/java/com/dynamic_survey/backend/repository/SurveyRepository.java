package com.dynamic_survey.backend.repository;

import com.dynamic_survey.backend.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * [教學說明] SurveyRepository
 */
@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    
    /**
     * [功能] 根據標題模糊搜尋問卷
     */
    List<Survey> findByTitleContainingIgnoreCase(String title);
    
    /**
     * [功能] 根據狀態查詢 (例如只查詢已發佈的)
     */
    List<Survey> findByStatus(Survey.Status status);

    /**
     * [教學說明] 自定義查詢 (Active Surveys)
     * 查詢條件：狀態為 PUBLISHED 且 當前日期在開始與結束日期之間。
     */
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Survey s WHERE s.status = 'PUBLISHED' " +
            "AND (s.startDate <= CURRENT_DATE AND s.endDate >= CURRENT_DATE)")
    List<Survey> findActiveSurveys();
}
