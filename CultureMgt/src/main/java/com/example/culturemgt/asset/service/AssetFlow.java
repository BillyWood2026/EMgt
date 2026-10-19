package com.example.culturemgt.asset.service;

import com.example.culturemgt.common.cul.CulBizException;

/**
 * 子模块8 文化资产与内刊 - 错误码与工具
 */
public class AssetFlow {

    public static final String EC = "E-AS";

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
