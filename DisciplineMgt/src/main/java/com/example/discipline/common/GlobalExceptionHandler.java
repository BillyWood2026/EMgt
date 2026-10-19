package com.example.discipline.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 统一异常处理。 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务规则/非法状态流转：返回 200 信封、code=500，前端按业务失败处理并展示中文原因 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseResult<Object> handleBiz(IllegalStateException ex) {
        ResponseResult<Object> body = new ResponseResult<>();
        body.setCode(500);
        body.setMessage(ex.getMessage());
        body.setMsg(ex.getMessage());
        body.setTimestamp(System.currentTimeMillis());
        return body;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseResult<Object> handleArg(IllegalArgumentException ex) {
        ResponseResult<Object> body = new ResponseResult<>();
        body.setCode(500);
        body.setMessage(ex.getMessage());
        body.setMsg(ex.getMessage());
        body.setTimestamp(System.currentTimeMillis());
        return body;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult<Object>> handleOther(Exception ex) {
        ResponseResult<Object> body = new ResponseResult<>();
        body.setCode(500);
        body.setMessage(ex.getMessage());
        body.setMsg(ex.getMessage());
        body.setTimestamp(System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
