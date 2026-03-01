import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Survey } from '../models/survey.model';
import { SurveyStats } from '../models/survey-stats.model';
import { map, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SurveyService {
  private http = inject(HttpClient);
  
  private readonly ADMIN_API_URL = 'http://localhost:8080/api/admin/surveys';
  private readonly PUBLIC_API_URL = 'http://localhost:8080/api/surveys';

  getActiveSurveys(): Observable<Survey[]> {
    return this.http.get<any>(this.PUBLIC_API_URL).pipe(map(res => res.data));
  }

  getAllSurveys(): Observable<Survey[]> {
    return this.http.get<any>(this.ADMIN_API_URL).pipe(map(res => res.data));
  }

  getAdminSurveyById(id: number): Observable<Survey> {
    return this.http.get<any>(`${this.ADMIN_API_URL}/${id}`).pipe(map(res => res.data));
  }

  getSurveyById(id: number): Observable<Survey> {
    return this.http.get<any>(`${this.PUBLIC_API_URL}/${id}/details`).pipe(map(res => res.data));
  }

  // === User Session API ===
  saveToSession(response: any): Observable<any> {
    return this.http.post<any>(`${this.PUBLIC_API_URL}/session-store`, response);
  }

  confirmSubmit(): Observable<any> {
    return this.http.post<any>(`${this.PUBLIC_API_URL}/confirm`, {});
  }

  // === Admin Session API ===
  saveAdminSurveyToSession(survey: Survey): Observable<any> {
    return this.http.post<any>(`${this.ADMIN_API_URL}/session-store`, survey);
  }

  getAdminSurveyFromSession(): Observable<Survey> {
    return this.http.get<any>(`${this.ADMIN_API_URL}/session-get`).pipe(map(res => res.data));
  }

  confirmAdminSubmit(isPublish: boolean): Observable<any> {
    return this.http.post<any>(`${this.ADMIN_API_URL}/confirm-commit?isPublish=${isPublish}`, {});
  }

  // === Base CRUD ===
  saveSurvey(survey: Survey): Observable<Survey> {
    const request = survey.id ? 
      this.http.put<any>(`${this.ADMIN_API_URL}/${survey.id}`, survey) :
      this.http.post<any>(this.ADMIN_API_URL, survey);
    return request.pipe(map(res => res.data));
  }

  deleteSurvey(id: number): Observable<void> {
    return this.http.delete<any>(`${this.ADMIN_API_URL}/${id}`).pipe(map(res => res.data));
  }

  getSurveyStats(id: number): Observable<SurveyStats> {
    return this.http.get<any>(`${this.ADMIN_API_URL}/${id}/stats`).pipe(map(res => res.data));
  }

  getUserHistory(): Observable<any[]> {
    return this.http.get<any>(`${this.PUBLIC_API_URL}/history`).pipe(map(res => res.data));
  }
}
