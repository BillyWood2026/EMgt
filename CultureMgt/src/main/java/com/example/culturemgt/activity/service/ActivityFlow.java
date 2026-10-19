package com.example.culturemgt.activity.service;

import com.example.culturemgt.common.cul.CulBizException;
import java.util.List;

/** 文化活动模块校验工具，错误码前缀 E-AC。 */
public final class ActivityFlow {

    public static final String EC = "E-AC";
    public static final List<String> ACT_TYPES = List.of(
            "文化宣讲", "价值观读书会", "奋斗者分享", "团建", "竞赛", "周年纪念", "新人融入");

    private ActivityFlow() {}

    public static void require(boolean cond, String code, String msg) {
        if (!cond) throw CulBizException.block(EC + "-" + code, msg);
    }

    public static int len(String s) {
        return s == null ? 0 : s.length();
    }
}
