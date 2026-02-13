import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthResponse, User } from '../models/auth.model';
import { tap } from 'rxjs';
import { Router } from '@angular/router';

/**
 * [教學說明] AuthService (認證服務)
 * -----------------------------------------------------------------------------
 * 負責處理所有的認證邏輯：登入、註冊、登出、儲存 Token。
 * 
 * [關鍵技術] Angular Signals
 * 我們使用 signal 來儲存當前的使用者資訊。
 * 這樣整個應用程式的任何組件都能「訂閱」這個狀態，當登入成功時 UI 會自動更新。
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  
  // API 基礎路徑 (實務上建議放在 environment.ts)
  private readonly API_URL = 'http://localhost:8080/api/auth';

  /**
   * [教學重點] currentUser Signal
   * signal<User | null>(null) 代表初始狀態為未登入 (null)。
   */
  currentUser = signal<User | null>(null);

  constructor() {
    // 初始化時檢查 localStorage 是否有 Token
    const token = localStorage.getItem('token');
    if (token) {
      // 若有 Token，嘗試取得使用者資訊以恢復登入狀態
      this.fetchUserProfile().subscribe({
        error: () => {
          // 若 Token 無效或使用者不存在，清除 Token
          localStorage.removeItem('token');
        }
      });
    }
  }

  /**
   * [功能] 登入
   * @param credentials 包含 email 與 password
   */
  login(credentials: any) {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap(res => {
        this.handleAuthSuccess(res.token);
      })
    );
  }

  /**
   * [功能] 註冊
   */
  register(userData: any) {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, userData).pipe(
      tap(res => {
        this.handleAuthSuccess(res.token);
      })
    );
  }

  /**
   * [功能] 登出
   */
  logout() {
    localStorage.removeItem('token');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * [私有輔助方法] 處理登入成功的邏輯
   */
  private handleAuthSuccess(token: string) {
    localStorage.setItem('token', token);
    // 登入後，呼叫 API 取得詳細 User 資訊
    this.fetchUserProfile().subscribe({
      next: () => this.router.navigate(['/']),
      error: () => {
        localStorage.removeItem('token');
        this.router.navigate(['/login']);
      }
    });
  }

  /**
   * [功能] 取得使用者個人資料
   */
  fetchUserProfile() {
    return this.http.get<User>('http://localhost:8080/api/users/me').pipe(
      tap(user => {
        this.currentUser.set(user); // 更新 Signal 狀態
      })
    );
  }
}
