# 動態問卷系統教學 (Tutorial Walkthrough)

本文件記錄專案開發的詳細流程，旨在作為教學素材。我們將從後端基礎建設開始，逐步構建一個完整的 Spring Boot + Angular 應用程式。

---

## Phase 1: 後端基礎與身份驗證 (Backend Infrastructure & Auth)

[... 前略 ...]

---

## Phase 2: 問卷管理系統 (Survey Management System)

[... 中略 ...]

---

## Phase 3: 前台使用者互動 (User Interaction)

[... 中略 ...]

---

## Phase 4: 數據統計與優化 (Data & Polish)

[... 步驟 20-21 ...]

### 步驟 22: 個人填寫紀錄 (User History)

**概念教學:**
為了提供更好的使用者體驗，我們讓使用者能查看自己的參與紀錄。
*   **後端識別**: 透過 JWT Token 獲取使用者 ID，並從 `Response` 表中查詢屬於該使用者的資料。
*   **表格呈現**: 使用 `MatTable` 列出問卷名稱與提交時間。
*   **日期格式化**: 使用 Angular 的 `date` pipe 將資料庫的 LocalDateTime 轉換為易讀的格式 (`yyyy-MM-dd HH:mm`)。

**實作動作:**
1.  建立 `ResponseHistoryDTO`。
2.  在 `PublicSurveyController` 實作 `/history` 接口。
3.  建立 `UserHistoryComponent` 與對應路由。

---

## 結語 (Conclusion)
恭喜！您已經完成了一個具備完整功能、現代架構且安全的 **動態問卷系統**。
在本教學中，我們涵蓋了：
*   **Spring Boot 3 + Security + JWT**: 完整的後端安全機制。
*   **JPA 一對多連動**: 處理複雜的巢狀資料結構。
*   **Angular 18 + Signals**: 現代化的前端狀態管理。
*   **Reactive Forms (FormArray)**: 處理動態、不固定數量的表單輸入。
*   **Chart.js**: 數據視覺化整合。

希望這份教學對您的開發與學習有所幫助！