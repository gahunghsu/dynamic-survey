package com.example.dynamicsurvey.repository;

import com.example.dynamicsurvey.entity.SurveyResponse;
import com.example.dynamicsurvey.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    List<SurveyResponse> findByUserOrderBySubmittedAtDesc(User user);
    List<SurveyResponse> findBySurveyId(Long surveyId);
    boolean existsBySurveyId(Long surveyId);
}
