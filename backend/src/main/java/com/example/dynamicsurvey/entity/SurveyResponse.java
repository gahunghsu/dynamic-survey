package com.example.dynamicsurvey.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * [教學說明] 問卷回覆實體 (Survey Response Entity)
 * -----------------------------------------------------------------------------
 * 【調整重點】
 * 為了支援「匿名/免登入作答」，我們將作答者的基本資訊直接儲存在回覆表中。
 */
@Entity
@Table(name = "survey_responses")
@Data
public class SurveyResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    // === 作答者基本資訊 (直接儲存在此，不強迫關聯 User 表) ===
    @Column(nullable = false)
    private String name;    // 姓名 (必填)

    @Column(nullable = false)
    private String phone;   // 手機 (必填)

    @Column(nullable = false) // 【修正】Email 改為必填
    private String email;

    @Column // 【修正】年齡改為選填
    private Integer age;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    /**
     * [可選] 關聯系統使用者
     * 若未來加入了註冊登入功能，可以透過此欄位連結到會員帳號。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResponseAnswer> answers = new ArrayList<>();
}
