package com.dynamic_survey.backend.repository;

import com.dynamic_survey.backend.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {
    // 查詢特定使用者的填寫紀錄
    List<Response> findByUserId(Long userId);

    // 檢查問卷是否有任何作答紀錄
    boolean existsBySurveyId(Long surveyId);
}
