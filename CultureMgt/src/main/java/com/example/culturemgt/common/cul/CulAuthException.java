package com.example.culturemgt.common.cul;

/** 文化模块权限异常，返回 403。 */
public class CulAuthException extends RuntimeException {
    public CulAuthException(String message) {
        super(message);
    }
}
