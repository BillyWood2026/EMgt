package com.example.culturemgt.foundation.service;

import com.example.culturemgt.common.cul.CulBizException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 文化底座通用校验与审批日志工具。 */
public final class FoundationFlow {

    public static final String EC = "E-CUL";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FoundationFlow() {
    }

    public static void require(boolean ok, String code, String message) {
        if (!ok) {
            throw CulBizException.block(EC + "-" + code, message);
        }
    }

    public static int len(String s) {
        return s == null ? 0 : s.trim().length();
    }

    public static void requireText(String v, int min, int max, String label, String code) {
        int n = len(v);
        require(n >= min && n <= max, code, label + "字数需在 " + min + "-" + max + " 字之间（当前 " + n + " 字）");
    }

    public static void requireDateRange(LocalDate effect, LocalDate expire, String code) {
        if (effect != null && expire != null) {
            require(!expire.isBefore(effect), code, "失效时间不得早于生效时间");
        }
    }

    /** 追加一条审批节点记录，返回新的 JSON 字符串。 */
    @SuppressWarnings("unchecked")
    public static String appendLog(String oldJson, Map<String, Object> entry) {
        List<Map<String, Object>> log = new ArrayList<>();
        if (oldJson != null && !oldJson.isBlank()) {
            try {
                log = MAPPER.readValue(oldJson, List.class);
            } catch (Exception ignore) {
                log = new ArrayList<>();
            }
        }
        log.add(new LinkedHashMap<>(entry));
        try {
            return MAPPER.writeValueAsString(log);
        } catch (Exception e) {
            throw new IllegalStateException("审批日志序列化失败", e);
        }
    }

    public static String bumpVersion(String version) {
        if (version == null || version.isBlank()) {
            return "V1.1";
        }
        String v = version.trim().replaceFirst("^[Vv]", "");
        String[] parts = v.split("\\.");
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return "V" + major + "." + (minor + 1);
        } catch (NumberFormatException e) {
            return version + ".1";
        }
    }
}
