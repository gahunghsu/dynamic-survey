package com.example.dynamicsurvey.repository;

import com.example.dynamicsurvey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * [教學說明] 問卷儲存庫 (Survey Repository)
 * -----------------------------------------------------------------------------
 * 目的：負責與資料庫進行交互。
 * 亮點：繼承 JpaRepository 即可自動獲得基本的 CRUD (新增、查詢、修改、刪除) 功能。
 */
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    
    /**
     * [教學重點] 自定義查詢 (Query Method)
     * 這裡使用了 JPQL 來查詢符合「已發佈」且「在有效日期內」的問卷。
     */
    @Query("SELECT s FROM Survey s WHERE s.status = 'PUBLISHED' AND s.startDate <= :today AND s.endDate >= :today")
    List<Survey> findActiveSurveys(@Param("today") LocalDate today);

    /**
     * [教學重點] 多條件動態篩選
     * 支援管理員根據標題關鍵字、日期區間進行搜尋。
     */
    @Query("SELECT s FROM Survey s WHERE " +
           "(:title IS NULL OR s.title LIKE %:title%) AND " +
           "(:startDate IS NULL OR s.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR s.endDate <= :endDate)")
    List<Survey> findByFilters(@Param("title") String title, 
                               @Param("startDate") LocalDate startDate, 
                               @Param("endDate") LocalDate endDate);
}
