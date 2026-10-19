package com.example.flowengine.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseResult<Void> handleBiz(BizException e) {
        log.warn("BizException: {}", e.getMessage());
        return ResponseResult.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseResult<Void> handleAll(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseResult.fail(500, "系统异常：" + e.getMessage());
    }
}
