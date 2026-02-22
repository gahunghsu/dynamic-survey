package com.example.dynamicsurvey.config;

import com.example.dynamicsurvey.vo.AppResponse;
import com.example.dynamicsurvey.vo.RspCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        // 抓取第一個錯誤訊息作為主訊息
        String firstMsg = errors.values().stream().findFirst().orElse("參數驗證失敗");
        
        return ResponseEntity.badRequest().body(AppResponse.error(RspCode.PARAM_ERROR, firstMsg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppResponse<String>> handleAllExceptions(Exception ex) {
        return ResponseEntity.internalServerError().body(AppResponse.error(RspCode.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
}
