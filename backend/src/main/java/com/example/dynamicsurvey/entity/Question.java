package com.example.dynamicsurvey.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * [教學說明] 題目實體類別 (Question Entity)
 * -----------------------------------------------------------------------------
 * 目的：對應資料庫中的 questions 資料表。
 */
@Entity
@Table(name = "questions")
@Data
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * [教學重點] 多對一關聯映射 (Question -> Survey)
     * 每一筆題目都必須屬於一個問卷。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(nullable = false, length = 75)
    private String title; // 題目內容

    @Column(nullable = false)
    private String type; // 類型：SINGLE (單選), MULTI (多選), TEXT (簡答)

    @Column(nullable = false)
    private boolean required; // 是否必填

    @Column(nullable = false)
    private int orderIndex; // 在問卷中的排列順序

    /**
     * [教學重點] 巢狀關聯 (Question -> Option)
     * 題目底下還有選項，同樣使用 CascadeType.ALL 實作連動儲存。
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Option> options = new ArrayList<>();
}
