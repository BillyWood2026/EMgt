package com.example.employee.config;

import com.example.common.dto.ResponseResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常响应：错误时仍返回 ResponseResult JSON({code,message,data,timestamp})，
 * 前端可通过 response.data.message 读取错误提示。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Void> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseResult.error(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseResult<Void> handleException(Exception e) {
        return ResponseResult.error(500, "服务器内部错误: " + e.getMessage());
    }
}
