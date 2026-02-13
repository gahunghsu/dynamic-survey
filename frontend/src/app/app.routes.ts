import { Routes } from '@angular/router';

/**
 * [教學說明] app.routes.ts (路由配置)
 * -----------------------------------------------------------------------------
 * 定義 URL 與組件之間的對應關係。
 */
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'home',
    loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'fill/:id',
    loadComponent: () => import('./pages/survey-fill/survey-fill.component').then(m => m.SurveyFillComponent)
  },
  {
    path: 'history',
    loadComponent: () => import('./pages/user-history/user-history.component').then(m => m.UserHistoryComponent)
  },
  {
    path: 'admin',
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/admin/survey-list/survey-list.component').then(m => m.SurveyListComponent)
      },
      {
        path: 'create', // 新增模式
        loadComponent: () => import('./pages/admin/survey-editor/survey-editor.component').then(m => m.SurveyEditorComponent)
      },
      {
        path: 'edit/:id', // 編輯模式 (帶 ID)
        loadComponent: () => import('./pages/admin/survey-editor/survey-editor.component').then(m => m.SurveyEditorComponent)
      },
      {
        path: 'stats/:id', // 統計模式
        loadComponent: () => import('./pages/admin/survey-stats/survey-stats.component').then(m => m.SurveyStatsComponent)
      }
    ]
  },
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  }
];
