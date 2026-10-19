package com.example.culturemgt.dashboard.service;

import com.example.culturemgt.common.cul.CulBizException;

/**
 * 子模块10 跨模块文化数据看板 - 错误码与工具（E-DB-01~04）
 * 01 钻取类型非法 02 越权组织访问 03 导出需二次确认 04 组织参数必填
 */
public class DashboardFlow {

    public static final String EC = "E-DB";

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
