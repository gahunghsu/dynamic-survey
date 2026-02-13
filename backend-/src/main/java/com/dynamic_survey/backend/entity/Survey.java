package com.dynamic_survey.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * [教學說明] Survey Entity (問卷實體)
 * -----------------------------------------------------------------------------
 * 這是問卷系統的核心實體，採用一對多 (One-to-Many) 的層級結構。
 * 一份問卷 (Survey) 包含多個題目 (Question)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "surveys")
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * [教學說明] Enumerated 狀態
     * DRAFT (草稿): 只有管理員看得到，且不能填寫。
     * PUBLISHED (已發佈): 前台可以看到並填寫。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    /**
     * [教學說明] One-to-Many 關聯 (問卷對題目)
     * 1. mappedBy = "survey": 指定關聯由 Question 類別中的 survey 欄位維護。
     * 2. cascade = CascadeType.ALL: 連動操作。當 Survey 儲存/更新/刪除時，旗下的 Questions 也會同步操作。
     * 3. orphanRemoval = true: 當題目從 list 中被移除時，資料庫中對應的紀錄也會被刪除。
     */
    @Builder.Default
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC") // 查詢時自動按順序排列
    private List<Question> questions = new ArrayList<>();

    public enum Status {
        DRAFT,
        PUBLISHED
    }
}
