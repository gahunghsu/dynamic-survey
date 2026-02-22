/**
 * [教學說明] Auth Models (認證相關資料模型)
 * -----------------------------------------------------------------------------
 * 在 TypeScript 中，我們使用 interface 來定義資料結構。
 * 這些結構必須與後端回傳的 DTO 一致。
 */

export interface User {
  id: number;
  email: string;
  name: string;
  phone?: string;
  role: 'USER' | 'ADMIN';
}

// 修改為對齊 AppResponse 結構
export interface AuthResponse {
  code: number;
  message: string;
  data: {
    token: string;
  };
}
