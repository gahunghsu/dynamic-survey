import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { authInterceptor } from './interceptors/auth.interceptor';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';

import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';

/**
 * [教學說明] app.config.ts (應用程式全域配置)
 * -----------------------------------------------------------------------------
 * 在 Standalone 模式下，我們在這裡註冊全域服務。
 * 1. provideHttpClient: 讓應用程式可以使用 HttpClient。
 * 2. withInterceptors([authInterceptor]): 註冊我們的 JWT 攔截器。
 * 3. provideCharts: 初始化 Chart.js 相關功能。
 * 4. providePrimeNG: 初始化 PrimeNG 18+ 主題 (Aura)。
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(
      withInterceptors([authInterceptor]) // 注入攔截器
    ),
    provideCharts(withDefaultRegisterables()),
    providePrimeNG({
      theme: {
        preset: Aura,
        options: {
          prefix: 'p',
          darkModeSelector: 'system',
          cssLayer: false
        }
      }
    })
  ]
};