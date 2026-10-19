package com.example.culturemgt.behavior.service;

import com.example.culturemgt.common.cul.CulBizException;

/**
 * 子模块7 文化行为与价值观履职 - 错误码与工具
 */
public class BehaviorFlow {

    public static final String EC = "E-BH";

    public static CulBizException block(String code, String msg) {
        return CulBizException.block(EC + "-" + code, msg);
    }

    public static CulBizException warn(String code, String msg) {
        return CulBizException.warn(EC + "-" + code, msg);
    }

    public static void require(boolean condition, String code, String msg) {
        if (!condition) {
            throw block(code, msg);
        }
    }
}
