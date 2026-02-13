import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Survey } from '../models/survey.model';
import { SurveyStats } from '../models/survey-stats.model';
import { Observable } from 'rxjs';

/**
 * [教學說明] SurveyService (問卷資料服務)
 * -----------------------------------------------------------------------------
 * 處理所有與問卷相關的 API 請求。
 * 由於我們在 app.config.ts 註冊了 authInterceptor，
 * 這裡的所有請求都會自動帶上 JWT Token。
 */
@Injectable({
  providedIn: 'root'
})
export class SurveyService {
  private http = inject(HttpClient);
  
  // 管理員專用的 API 路徑
  private readonly ADMIN_API_URL = 'http://localhost:8080/api/admin/surveys';
  // 前台公開的 API 路徑
  private readonly PUBLIC_API_URL = 'http://localhost:8080/api/surveys';

  /**
   * [功能] 取得所有可填寫的問卷列表 (前台)
   */
  getActiveSurveys(): Observable<Survey[]> {
    return this.http.get<Survey[]>(this.PUBLIC_API_URL);
  }

  /**
   * [功能] 取得所有問卷列表 (管理員)
   */
  getAllSurveys(): Observable<Survey[]> {
    return this.http.get<Survey[]>(this.ADMIN_API_URL);
  }

  /**
   * [功能] 取得問卷詳情 (管理員編輯用)
   */
  getAdminSurveyById(id: number): Observable<Survey> {
    return this.http.get<Survey>(`${this.ADMIN_API_URL}/${id}`);
  }

  /**
   * [功能] 取得問卷詳情 (填寫用)
   */
  getSurveyById(id: number): Observable<Survey> {
    return this.http.get<Survey>(`${this.PUBLIC_API_URL}/${id}`);
  }

  /**
   * [功能] 提交問卷答案
   */
  submitResponse(surveyId: number, response: any): Observable<void> {
    return this.http.post<void>(`${this.PUBLIC_API_URL}/${surveyId}/submit`, response);
  }

  /**
   * [功能] 儲存問卷 (新增或更新)
   */
  saveSurvey(survey: Survey): Observable<Survey> {
    if (survey.id) {
      return this.http.put<Survey>(`${this.ADMIN_API_URL}/${survey.id}`, survey);
    } else {
      return this.http.post<Survey>(this.ADMIN_API_URL, survey);
    }
  }

  /**
   * [功能] 刪除問卷
   */
  deleteSurvey(id: number): Observable<void> {
    return this.http.delete<void>(`${this.ADMIN_API_URL}/${id}`);
  }

  /**
   * [功能] 取得問卷統計數據 (管理員使用)
   */
  getSurveyStats(id: number): Observable<SurveyStats> {
    return this.http.get<SurveyStats>(`${this.ADMIN_API_URL}/${id}/stats`);
  }

  /**
   * [功能] 取得個人填寫紀錄 (前台)
   */
  getUserHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.PUBLIC_API_URL}/history`);
  }
}