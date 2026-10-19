package com.example.datamgt.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/data/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "DataMgt", "module", "集中数据管理平台");
    }
}
