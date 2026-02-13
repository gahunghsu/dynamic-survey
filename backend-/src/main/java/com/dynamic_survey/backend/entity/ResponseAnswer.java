package com.dynamic_survey.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [教學說明] ResponseAnswer Entity (具體答案)
 * -----------------------------------------------------------------------------
 * 紀錄每一題的填寫結果。
 * 1. 如果是單選/多選題：儲存 option_id。
 * 2. 如果是簡答題：儲存 answer_text。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "response_answers")
public class ResponseAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private Response response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * [教學說明] 選項關聯
     * 如果是選擇題，這裡會紀錄所選的選項 ID。
     * 多選題會產生多筆 ResponseAnswer 紀錄，每筆對應一個被勾選的選項。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private Option option;

    /**
     * [教學說明] 簡答文字
     * 如果是簡答題，答案會存在這裡。
     */
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;
}
