package com.example.improvemgt.config;

import com.example.common.dto.ResponseResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：业务非法参数/状态 -> HTTP 400，响应体 code=500
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ResponseResult<Void>> handleBusiness(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseResult.error(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseResult<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseResult.error("数据完整性约束冲突: " + ex.getMostSpecificCause().getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult<Void>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseResult.error("服务器内部错误: " + ex.getMessage()));
    }
}
