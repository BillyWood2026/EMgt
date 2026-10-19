package com.example.sysmonitor.common;

import com.example.common.dto.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseResult<Void> handleBiz(BizException e) {
        log.warn("BizException: {}", e.getMessage());
        return ResponseResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseResult<Void> handleAll(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseResult.error("系统异常：" + e.getMessage());
    }
}
