package com.example.qualification.common;

import com.example.common.dto.ResponseResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：BizException 及其他异常统一返回 ResponseResult
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseResult<Void> handleBiz(BizException e) {
        return ResponseResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseResult<Void> handleException(Exception e) {
        return ResponseResult.error(e.getMessage());
    }
}
