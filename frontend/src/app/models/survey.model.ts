/**
 * [教學說明] Survey Models (問卷相關資料模型)
 * -----------------------------------------------------------------------------
 * 這裡的結構必須與後端的 SurveyDTO 完全對應。
 */

export type SurveyStatus = 'DRAFT' | 'PUBLISHED';
export type QuestionType = 'SINGLE' | 'MULTI' | 'TEXT';

export interface Option {
  id?: number;
  optionText: string;
  orderIndex: number;
}

export interface Question {
  id?: number;
  title: string;
  type: QuestionType;
  required: boolean;
  orderIndex: number;
  options: Option[];
}

export interface Survey {
  id?: number;
  title: string;
  description: string;
  startDate: string; // ISO date string
  endDate: string;   // ISO date string
  status: SurveyStatus;
  questions: Question[];
}
