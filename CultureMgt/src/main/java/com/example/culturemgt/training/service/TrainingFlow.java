package com.example.culturemgt.training.service;

import com.example.culturemgt.common.cul.CulBizException;

/** 在线培训及考试校验工具，错误码前缀 E-TR。 */
public final class TrainingFlow {

    public static final String EC = "E-TR";

    private TrainingFlow() {
    }

    public static void require(boolean ok, String code, String message) {
        if (!ok) {
            throw CulBizException.block(EC + "-" + code, message);
        }
    }

    public static int len(String s) {
        return s == null ? 0 : s.trim().length();
    }
}
