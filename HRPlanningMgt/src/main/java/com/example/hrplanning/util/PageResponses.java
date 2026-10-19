package com.example.hrplanning.util;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 构造前端约定的精确分页 JSON：{records, total, current, size, pages}
 * 前端 hr-planning/Index.vue 的 extractPage 直接从响应体顶层读取 records/total。
 */
public final class PageResponses {

    private PageResponses() {
    }

    public static <T> Map<String, Object> of(IPage<T> page) {
        Map<String, Object> body = new LinkedHashMap<>(5);
        body.put("records", page.getRecords());
        body.put("total", page.getTotal());
        body.put("current", page.getCurrent());
        body.put("size", page.getSize());
        body.put("pages", page.getPages());
        return body;
    }
}
