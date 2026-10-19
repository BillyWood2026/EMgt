package com.example.filemgt.common;

import lombok.Data;

@Data
public class ResponseResult<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ResponseResult<T> ok(T data) {
        ResponseResult<T> r = new ResponseResult<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> ResponseResult<T> ok() {
        return ok(null);
    }

    public static <T> ResponseResult<T> fail(int code, String message) {
        ResponseResult<T> r = new ResponseResult<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> ResponseResult<T> fail(String message) {
        return fail(500, message);
    }
}
