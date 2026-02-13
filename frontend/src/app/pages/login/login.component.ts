import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';

/**
 * [教學說明] LoginComponent (登入頁面)
 * -----------------------------------------------------------------------------
 * 展示了：
 * 1. Reactive Forms: 處理表單驗證 (Validators.required, Validators.email)。
 * 2. Angular Material: 使用 MatCard, MatInput 快速構建美觀介面。
 * 3. AuthService 串接: 呼叫 login API。
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);

  // 定義表單結構與驗證規則
  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  onSubmit() {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: () => {
          this.snackBar.open('登入成功', '關閉', { duration: 3000 });
        },
        error: (err) => {
          this.snackBar.open('登入失敗，請檢查帳號密碼', '關閉', { duration: 3000 });
        }
      });
    }
  }
}
