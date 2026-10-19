package com.example.occhealthmgt.common;

import lombok.Data;

@Data
public class ResponseResult<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> r = new ResponseResult<>();
        r.code = 200; r.message = "success"; r.data = data;
        return r;
    }

    public static ResponseResult<Void> success() {
        return success(null);
    }

    public static <T> ResponseResult<T> error(int code, String message) {
        ResponseResult<T> r = new ResponseResult<>();
        r.code = code; r.message = message;
        return r;
    }
}
