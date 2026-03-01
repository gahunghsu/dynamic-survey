import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthResponse, User } from '../models/auth.model';
import { map, tap } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  
  private readonly API_URL = 'http://localhost:8080/api/auth';

  currentUser = signal<User | null>(null);

  constructor() {
    const token = localStorage.getItem('token');
    if (token) {
      this.fetchUserProfile().subscribe({
        error: () => {
          localStorage.removeItem('token');
        }
      });
    }
  }

  /**
   * [修正] 提取 res.data.token
   */
  login(credentials: any) {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap(res => {
        if (res.code === 200 && res.data.token) {
          this.handleAuthSuccess(res.data.token);
        }
      })
    );
  }

  /**
   * [修正] 提取 res.data.token
   */
  register(userData: any) {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, userData).pipe(
      tap(res => {
        if (res.code === 200 && res.data.token) {
          this.handleAuthSuccess(res.data.token);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  private handleAuthSuccess(token: string) {
    localStorage.setItem('token', token);
    this.fetchUserProfile().subscribe({
      next: () => this.router.navigate(['/']),
      error: () => {
        localStorage.removeItem('token');
        this.router.navigate(['/login']);
      }
    });
  }

  fetchUserProfile() {
    // 增加時間戳記防止瀏覽器快取含有 401 的 200 OK AppResponse (導致怎麼登入都抓到舊的 401 null data)
    return this.http.get<any>(`http://localhost:8080/api/users/profile?t=${new Date().getTime()}`).pipe(
      map(res => {
        if (res.code !== 200) {
          throw new Error(res.message || '無法取得使用者資料');
        }
        return res.data as User;
      }),
      tap(user => {
        this.currentUser.set(user);
      })
    );
  }
}
