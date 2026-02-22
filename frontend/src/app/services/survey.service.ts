import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Survey } from '../models/survey.model';
import { SurveyStats } from '../models/survey-stats.model';
import { map, Observable } from 'rxjs';

/**
 * [修正版] SurveyService
 * 確保所有方法都正確從 AppResponse.data 中提取資料
 */
@Injectable({
  providedIn: 'root'
})
export class SurveyService {
  private http = inject(HttpClient);
  
  private readonly ADMIN_API_URL = 'http://localhost:8080/api/admin/surveys';
  private readonly PUBLIC_API_URL = 'http://localhost:8080/api/surveys';

  /**
   * [修正] 取得所有可填寫的問卷列表 (前台)
   */
  getActiveSurveys(): Observable<Survey[]> {
    return this.http.get<any>(this.PUBLIC_API_URL).pipe(
      map(res => res.data as Survey[])
    );
  }

  /**
   * [修正] 取得所有問卷列表 (管理員)
   */
  getAllSurveys(): Observable<Survey[]> {
    return this.http.get<any>(this.ADMIN_API_URL).pipe(
      map(res => res.data as Survey[])
    );
  }

  /**
   * [修正] 取得問卷詳情 (管理員編輯用)
   */
  getAdminSurveyById(id: number): Observable<Survey> {
    return this.http.get<any>(`${this.ADMIN_API_URL}/${id}`).pipe(
      map(res => res.data as Survey)
    );
  }

  /**
   * [修正] 取得問卷詳情 (填寫用)
   */
  getSurveyById(id: number): Observable<Survey> {
    return this.http.get<any>(`${this.PUBLIC_API_URL}/${id}/details`).pipe(
      map(res => res.data as Survey)
    );
  }

  /**
   * [修正] 提交問卷答案
   */
  submitResponse(surveyId: number, response: any): Observable<void> {
    return this.http.post<any>(`${this.PUBLIC_API_URL}/${surveyId}/submit`, response).pipe(
      map(res => res.data)
    );
  }

  /**
   * [修正] 儲存問卷 (新增或更新)
   */
  saveSurvey(survey: Survey): Observable<Survey> {
    const request = survey.id ? 
      this.http.put<any>(`${this.ADMIN_API_URL}/${survey.id}`, survey) :
      this.http.post<any>(this.ADMIN_API_URL, survey);
    
    return request.pipe(map(res => res.data as Survey));
  }

  /**
   * [修正] 刪除問卷
   */
  deleteSurvey(id: number): Observable<void> {
    return this.http.delete<any>(`${this.ADMIN_API_URL}/${id}`).pipe(
      map(res => res.data)
    );
  }

  /**
   * [修正] 取得問卷統計數據 (管理員使用)
   */
  getSurveyStats(id: number): Observable<SurveyStats> {
    return this.http.get<any>(`${this.ADMIN_API_URL}/${id}/stats`).pipe(
      map(res => res.data as SurveyStats)
    );
  }

  /**
   * [修正] 取得個人填寫紀錄 (前台)
   */
  getUserHistory(): Observable<any[]> {
    return this.http.get<any>(`${this.PUBLIC_API_URL}/history`).pipe(
      map(res => res.data as any[])
    );
  }
}
