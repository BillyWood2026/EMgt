package com.example.partyaffairsmgt.common.party;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 从当前请求读取 Header / 用户标识。 */
public final class CurrentHeader {

    private CurrentHeader() {
    }

    public static String get(String name) {
        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) {
            return null;
        }
        return attr.getRequest().getHeader(name);
    }

    /** 当前操作人（X-User-Id），用于审计与留痕。 */
    public static String operator() {
        String uid = get("X-User-Id");
        return uid == null || uid.isBlank() ? "anonymous" : uid;
    }
}
