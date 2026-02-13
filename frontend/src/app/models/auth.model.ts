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

export interface AuthResponse {
  token: string;
}
