package com.example.culturemgt.common;

import com.example.culturemgt.common.cul.CulAuthException;
import com.example.culturemgt.common.cul.CulBizException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 统一业务/权限异常处理：业务阻断 HTTP 400，权限不足 HTTP 403。 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CulBizException.class)
    public ResponseEntity<ResponseResult<Map<String, String>>> handleBiz(CulBizException ex) {
        Map<String, String> extra = new LinkedHashMap<>();
        extra.put("errorCode", ex.getErrorCode());
        extra.put("level", ex.getLevel());
        ResponseResult<Map<String, String>> body = new ResponseResult<>();
        body.setCode(400);
        body.setMessage(ex.getMessage());
        body.setMsg("[" + ex.getErrorCode() + "][" + ex.getLevel() + "] " + ex.getMessage());
        body.setTimestamp(System.currentTimeMillis());
        body.setData(extra);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(CulAuthException.class)
    public ResponseEntity<ResponseResult<Object>> handleAuth(CulAuthException ex) {
        ResponseResult<Object> body = new ResponseResult<>();
        body.setCode(403);
        body.setMessage(ex.getMessage());
        body.setMsg(ex.getMessage());
        body.setTimestamp(System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
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
