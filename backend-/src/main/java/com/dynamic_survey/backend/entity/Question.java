package com.dynamic_survey.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * [教學說明] Question Entity (題目實體)
 * -----------------------------------------------------------------------------
 * 題目屬於特定的問卷，且可能擁有多個選項 (如果是選擇題的話)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(name = "is_required")
    private boolean required;

    /**
     * [教學說明] orderIndex
     * 用於記錄題目在問卷中的顯示順序。
     */
    @Column(name = "order_index")
    private int orderIndex;

    /**
     * [教學說明] Many-to-One 關聯 (題目對問卷)
     * @JsonIgnore: 防止 JSON 序列化時產生循環引用 (Survey -> Question -> Survey...)。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    @JsonIgnore
    private Survey survey;

    /**
     * [教學說明] One-to-Many 關聯 (題目對選項)
     * 只有當 Type 為 SINGLE (單選) 或 MULTI (多選) 時，此清單才有資料。
     */
    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Option> options = new ArrayList<>();

    public enum Type {
        SINGLE, // 單選題
        MULTI,  // 多選題
        TEXT    // 簡答題
    }
}
