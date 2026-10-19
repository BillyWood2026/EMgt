package com.example.culturemgt.honor.service;

import com.example.culturemgt.common.cul.CulBizException;

/** 荣誉与榜样校验工具，错误码前缀 E-HO。 */
public final class HonorFlow {

    public static final String EC = "E-HO";

    private HonorFlow() {
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
