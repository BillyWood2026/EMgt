package com.example.culturemgt.common.cul;

import lombok.Getter;

/**
 * CCMS 业务异常：携带异常编码（如 E-CUL-01）与级别（阻断/预警/告警）。
 */
@Getter
public class CulBizException extends RuntimeException {

    /** BLOCK 阻断 / WARN 预警 / ALERT 告警 */
    public static final String BLOCK = "阻断";
    public static final String WARN = "预警";
    public static final String ALERT = "告警";

    private final String errorCode;
    private final String level;

    public CulBizException(String errorCode, String level, String message) {
        super(message);
        this.errorCode = errorCode;
        this.level = level;
    }

    public static CulBizException block(String errorCode, String message) {
        return new CulBizException(errorCode, BLOCK, message);
    }

    public static CulBizException warn(String errorCode, String message) {
        return new CulBizException(errorCode, WARN, message);
    }
}
