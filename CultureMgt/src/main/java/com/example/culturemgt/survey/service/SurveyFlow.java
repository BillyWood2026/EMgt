package com.example.culturemgt.survey.service;

import com.example.culturemgt.common.cul.CulBizException;

/** 文化调研与氛围评估校验工具，错误码前缀 E-SR。 */
public final class SurveyFlow {

    public static final String EC = "E-SR";

    private SurveyFlow() {
    }

    public static void require(boolean ok, String code, String message) {
        if (!ok) {
            throw CulBizException.block(EC + "-" + code, message);
        }
    }

    public static int len(String s) {
        return s == null ? 0 : s.trim().length();
    }

    /** 算法61 氛围指数分级：优秀≥85；良好70-84；一般60-69；薄弱<60。 */
    public static String grade(double index) {
        if (index >= 85) {
            return "优秀";
        }
        if (index >= 70) {
            return "良好";
        }
        if (index >= 60) {
            return "一般";
        }
        return "薄弱";
    }

    /** 风险等级：薄弱=高风险，一般=中风险，良好/优秀=低风险。 */
    public static String riskLevel(String grade) {
        return switch (grade) {
            case "薄弱" -> "高风险";
            case "一般" -> "中风险";
            default -> "低风险";
        };
    }
}
