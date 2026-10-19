package com.example.partyaffairsmgt.common;

import lombok.Data;

@Data
public class ResponseResult<T> {

    private int code;
    private String message;
    private T data;
    private long timestamp;
    private String msg;

    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> r = new ResponseResult<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        r.timestamp = System.currentTimeMillis();
        r.msg = "success";
        return r;
    }

    public static <T> ResponseResult<T> error(String message) {
        ResponseResult<T> r = new ResponseResult<>();
        r.code = 500;
        r.message = message;
        r.timestamp = System.currentTimeMillis();
        r.msg = message;
        return r;
    }
}
