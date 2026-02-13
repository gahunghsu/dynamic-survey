# 汶欣動態問卷系統 - 開發規格書 (Development Specification)

本文件根據「汶欣的動態問卷」需求，規劃完整的前後端開發規格。

## 1. 專案概述 (Project Overview)

本系統為一個動態問卷平台，分為 **「前台 (使用者端)」** 與 **「後台 (管理員端)」**。
*   **後台**：提供管理者進行問卷的 CRUD（建立、讀取、更新、刪除）、題目設計、數據統計與結果分析。
*   **前台**：提供使用者註冊、登入、瀏覽問卷列表、填寫問卷、查看填寫紀錄及修改個人資料。

## 2. 技術堆疊 (Tech Stack)

*   **Frontend (前端):**
    *   **Framework:** Angular 18+ (Standalone Components, Signals)
    *   **Styling:** Tailwind CSS (Layout & Utilities) + Angular Material (UI Components)
    *   **Charts:** Ng2-charts / Chart.js (用於統計圖表)
*   **Backend (後端):**
    *   **Framework:** Java 21 + Spring Boot 3 (Gradle)
    *   **Features:** Spring Boot DevTools (Hot Restart enabled)
    *   **Security:** Spring Security + JWT (JSON Web Token) + Global CORS Configuration
    *   **Database:** MySQL 8.0
    *   **ORM:** Spring Data JPA (Hibernate)

---

## 3. 資料庫設計 (Database Schema - MySQL)

### 3.1 核心資料表 (Tables)

| 資料表 | 說明 | 關鍵欄位 |
| :--- | :--- | :--- |
| **Users** | 使用者/管理員 | id, email, password (BCrypt), name, phone, role (ADMIN/USER) |
| **Surveys** | 問卷基本資料 | id, title, description, start_date, end_date, status (DRAFT/PUBLISHED) |
| **Questions** | 問卷題目 | id, survey_id, title, type (SINGLE/MULTI/TEXT), is_required, order_index |
| **Options** | 選擇題選項 | id, question_id, option_text, order_index |
| **Responses** | 問卷填寫紀錄 | id, survey_id, user_id, submitted_at |
| **ResponseAnswers** | 具體填寫答案 | id, response_id, question_id, option_id (Null for Text), answer_text (Null for Options) |

---

## 4. 後端 API 規格 (Backend Specifications)

### 4.1 驗證與使用者 (Auth & User)
*   `POST /api/auth/register`: 註冊新帳號。
*   `POST /api/auth/login`: 登入，回傳 JWT Token。
*   `GET /api/users/profile`: 取得當前使用者資料。
*   `PUT /api/users/profile`: 修改會員資料 (密碼、手機)。

### 4.2 問卷管理 (Admin Survey Management)
*   `GET /api/admin/surveys`: 搜尋問卷列表 (分頁、名稱、日期篩選)。
*   `POST /api/admin/surveys`: 建立新問卷 (Basic Info)。
*   `PUT /api/admin/surveys/{id}`: 更新問卷、題目與選項。
*   `DELETE /api/admin/surveys/{id}`: 刪除問卷 (連動刪除相關資料)。

### 4.3 問卷統計 (Admin Statistics)
*   `GET /api/admin/surveys/{id}/stats`: 取得統計數據 (百分比、簡答列表)。
*   `GET /api/admin/surveys/{id}/responses`: 取得填寫者列表。

### 4.4 前台流程 (User Flow)
*   `GET /api/surveys`: 取得公開問卷列表。
*   `GET /api/surveys/{id}/details`: 取得填寫用的問卷結構。
*   `POST /api/surveys/{id}/submit`: 提交問卷答案。
*   `GET /api/surveys/history`: 取得個人填寫紀錄。

---

## 5. 前端架構設計 (Frontend Architecture)

### 5.1 路由規劃 (Routing)
*   `/auth/login`, `/auth/register`
*   `/admin/dashboard`: 後台列表
*   `/admin/edit/:id`: 編輯/新增問卷 (使用 Stepper)
*   `/admin/stats/:id`: 統計圖表
*   `/user/home`: 前台列表
*   `/user/fill/:id`: 填寫頁面 (含確認步驟)
*   `/user/history`: 個人填寫紀錄

### 5.2 關鍵開發要點
1.  **動態表單 (Dynamic Forms):** 使用 Angular `Reactive Forms` (FormArray) 處理不固定數量的題目與選項。
2.  **狀態管理 (Signals):** 使用 Angular Signals 管理問卷編輯狀態與列表數據。
3.  **UI 組件:**
    *   `MatTable`: 用於所有清單顯示。
    *   `MatDatepicker`: 日期區間選擇。
    *   `MatDialog`: 確認刪除、送出成功彈窗。
4.  **樣式控制:** 使用 Tailwind CSS 實作 PDF 中的米色與深褐色主題感。

---

## 6. 預計實作階段 (Phases)
1.  **Phase 1:** 環境架設、DB Schema 建立與 JWT 認證機制。
2.  **Phase 2:** 後台問卷 CRUD 與動態題目編輯器。
3.  **Phase 3:** 前台問卷渲染、填寫邏輯與歷史紀錄。
4.  **Phase 4:** Chart.js 統計圖表整合與 UI/UX 調校。
