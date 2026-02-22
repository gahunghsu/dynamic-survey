package com.example.dynamicsurvey.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * [教學說明] 問卷實體類別 (Survey Entity)
 * -----------------------------------------------------------------------------
 * 目的：對應資料庫中的 surveys 資料表。
 * 亮點：使用 JPA 的關聯映射處理「一對多」關係。
 */
@Entity
@Table(name = "surveys")
@Data
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自動遞增 ID
    private Long id;

    @Column(nullable = false, length = 50) // 標題，長度限制 50
    private String title;

    @Column(length = 300) // 說明，長度限制 300
    private String description;

    @Column(nullable = false)
    private LocalDate startDate; // 開始日期

    @Column(nullable = false)
    private LocalDate endDate; // 結束日期

    @Column(nullable = false)
    private String status; // 狀態：DRAFT (草稿), PUBLISHED (已發佈)

    /**
     * [教學重點] 一對多關聯映射 (Survey -> Question)
     * - mappedBy: 指向 Question 類別中的 survey 屬性
     * - cascade = ALL: 當儲存 Survey 時，底下的 Questions 也會一併儲存
     * - orphanRemoval = true: 當題目從清單中移除時，資料庫也會自動刪除該筆資料
     */
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC") // 查詢時根據 orderIndex 排序
    private List<Question> questions = new ArrayList<>();
}
