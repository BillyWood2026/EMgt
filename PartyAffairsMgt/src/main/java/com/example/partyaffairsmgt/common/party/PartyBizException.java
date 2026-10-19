package com.example.partyaffairsmgt.common.party;

import lombok.Getter;

/**
 * 党组织管理业务异常：携带异常编码（如 E-ORG-01）与级别（阻断/预警/告警）。
 */
@Getter
public class PartyBizException extends RuntimeException {

    /** BLOCK 阻断 / WARN 预警 / ALERT 告警 */
    public static final String BLOCK = "阻断";
    public static final String WARN = "预警";
    public static final String ALERT = "告警";

    private final String errorCode;
    private final String level;

    public PartyBizException(String errorCode, String level, String message) {
        super(message);
        this.errorCode = errorCode;
        this.level = level;
    }

    public static PartyBizException block(String errorCode, String message) {
        return new PartyBizException(errorCode, BLOCK, message);
    }
}
