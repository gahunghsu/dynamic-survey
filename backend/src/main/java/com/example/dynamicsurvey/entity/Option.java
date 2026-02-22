package com.example.dynamicsurvey.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * [教學說明] 選項實體類別 (Option Entity)
 * -----------------------------------------------------------------------------
 * 目的：對應資料庫中的 options 資料表，存放選擇題的選項內容。
 */
@Entity
@Table(name = "options")
@Data
public class Option {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * [教學重點] 選項屬於特定題目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private String optionText; // 選項文字內容

    @Column(nullable = false)
    private int orderIndex; // 選項順序
}
