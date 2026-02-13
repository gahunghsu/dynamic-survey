import { HttpInterceptorFn } from '@angular/common/http';

/**
 * [教學說明] AuthInterceptor (認證攔截器)
 * -----------------------------------------------------------------------------
 * 攔截器就像是一個「關卡」，所有的 HTTP 請求在送出前都會經過這裡。
 * 
 * 它的任務是：
 * 如果 localStorage 裡有 Token，就把它塞進 Header 的 "Authorization: Bearer <token>"。
 * 這樣我們就不需要在每個 Service 裡手動加 Header 了。
 * 
 * [關鍵技術] Functional Interceptor
 * 這是 Angular 17+ 推薦的寫法，比以前的 Class-based Interceptor 更簡潔。
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');

  // 如果有 Token，就 Clone (複製) 請求並加入 Header
  // 注意：req 物件是不可變的 (Immutable)，所以必須用 clone()
  if (token) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(authReq);
  }

  // 如果沒 Token，就直接傳遞原始請求
  return next(req);
};
