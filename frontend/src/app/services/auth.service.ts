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

  /**
   * [修正] 路徑改為 /profile 並對齊 AppResponse 結構
   */
  fetchUserProfile() {
    return this.http.get<any>('http://localhost:8080/api/users/profile').pipe(
      map(res => res.data as User), // 從 data 欄位提取 User 物件
      tap(user => {
        this.currentUser.set(user);
      })
    );
  }
}
