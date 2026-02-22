# 動態問卷系統 - 後端開發實戰教學 (Walkthrough)

本文件將引導您如何從零開始建置動態問卷系統的後端，遵循 Spring Boot 三層式架構與業界標準。

---

## 1. 技術棧與環境準備
- **語言**: Java 17
- **框架**: Spring Boot 3.2.2
- **資料庫**: MySQL
- **核心庫**: Spring Data JPA, Spring Security, Validation (JSR-380), Lombok, JWT (jjwt)

## 2. 專案架構設計 (Three-Tier Architecture)
我們採用標準的三層架構，確保程式碼的可維護性與擴展性：
- **Entity**: 對應資料庫表。
- **Repository**: 資料存取層 (Spring Data JPA)。
- **Service**: 核心業務邏輯。
- **Controller**: REST API 接口。
- **VO/DTO**: 資料傳輸物件，用於請求驗證與回應格式化。

## 3. 核心功能實作步驟

### Step 1: 統一回應格式 (SB04 規範)
為了讓前端更容易處理回應，我們定義了 `RspCode` (Enum) 與 `AppResponse<T>`。
- **目的**: 確保所有 API 回傳格式一致 (code, message, data)。
- **技術點**: 使用 Generics `<T>` 支援多種資料類型。

### Step 2: 資料模型設計 (Entity)
我們設計了五個核心實體：
1. `User`: 使用者帳號，區分 `ADMIN` 與 `USER` 角色。
2. `Survey`: 問卷主體。
3. `Question`: 題目，關聯至問卷。
4. `Option`: 選項，關聯至題目。
5. `SurveyResponse` & `Answer`: 紀錄使用者的作答內容。

### Step 3: 安全與認證 (Security & JWT)
系統採用 **Stateless JWT** 認證：
- **BCryptPasswordEncoder**: 加密存儲使用者密碼。
- **JwtUtils**: 負責 Token 的生成、解析與驗證。
- **AuthTokenFilter**: 攔截請求，驗證 Header 中的 `Authorization: Bearer <token>`。
- **WebSecurityConfig**: 設定 API 的公開權限 (Login/Register) 與保護路徑 (Admin API)。

### Step 4: 業務邏輯與分組驗證 (Service & Validation)
在 `SurveyService` 中，我們實作了複雜的保存邏輯：
- **Cascade**: 使用 `CascadeType.ALL`，儲存問卷時會同步更新題目與選項。
- **Validation**: 在 DTO 上使用 `@NotBlank`, `@Size`, `@NotNull` 確保資料正確性。
- **統計邏輯**: 透過聚合 `SurveyResponse` 的數據，計算每個選項的填寫次數與百分比。

## 4. API 測試指引
可以使用 Postman 進行以下測試：
1. **註冊/登入**: `POST /api/auth/register` 取得 Token。
2. **前台獲取問卷**: `GET /api/surveys`。
3. **提交問卷**: `POST /api/surveys/{id}/submit` (需帶 Token)。
4. **管理員管理**: `POST /api/admin/surveys` (需 ADMIN 角色)。

---
*本教學由動態問卷系統專案小組製作。*
