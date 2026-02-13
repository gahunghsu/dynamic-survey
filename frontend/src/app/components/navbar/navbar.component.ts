import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';

/**
 * [教學說明] NavbarComponent (導覽列)
 * -----------------------------------------------------------------------------
 * 1. 使用 AuthService 的 currentUser Signal。
 * 2. 透過 @if 語法根據登入狀態動態切換顯示內容。
 * 3. 實作登出邏輯。
 */
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  // 注入 AuthService，以便讀取使用者狀態
  authService = inject(AuthService);

  logout() {
    this.authService.logout();
  }
}