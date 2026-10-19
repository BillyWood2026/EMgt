package com.example.culturemgt.casehub.service;

import com.example.culturemgt.common.cul.CulBizException;

/** 案例库校验工具，错误码前缀 E-CC。 */
public final class CaseFlow {

    public static final String EC = "E-CC";

    private CaseFlow() {
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
