package com.example.partyaffairsmgt.common;

import com.example.partyaffairsmgt.common.party.PartyAuthException;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 统一业务/权限异常处理：业务阻断 HTTP 400，权限不足 HTTP 403。 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PartyBizException.class)
    public ResponseEntity<ResponseResult<Map<String, String>>> handleBiz(PartyBizException ex) {
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

    @ExceptionHandler(PartyAuthException.class)
    public ResponseEntity<ResponseResult<Object>> handleAuth(PartyAuthException ex) {
        ResponseResult<Object> body = new ResponseResult<>();
        body.setCode(403);
        body.setMessage(ex.getMessage());
        body.setMsg(ex.getMessage());
        body.setTimestamp(System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /** 请求方法不支持：返回 405 而非落入 500。 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseResult<Object>> handleMethod(HttpRequestMethodNotSupportedException ex) {
        ResponseResult<Object> body = new ResponseResult<>();
        body.setCode(405);
        body.setMessage(ex.getMessage());
        body.setMsg(ex.getMessage());
        body.setTimestamp(System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    /** 缺少必填请求参数：返回 400 而非 500。 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseResult<Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        ResponseResult<Object> body = new ResponseResult<>();
        body.setCode(400);
        body.setMessage("缺少必填参数：" + ex.getParameterName());
        body.setMsg(body.getMessage());
        body.setTimestamp(System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** 请求体反序列化失败（JSON/时间格式错误）：返回 400 而非 500。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseResult<Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        ResponseResult<Object> body = new ResponseResult<>();
        body.setCode(400);
        body.setMessage("请求参数格式错误：" + ex.getMostSpecificCause().getMessage());
        body.setMsg(body.getMessage());
        body.setTimestamp(System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
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
