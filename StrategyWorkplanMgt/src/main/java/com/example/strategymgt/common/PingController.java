package com.example.strategymgt.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/strategy/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "StrategyWorkplanMgt", "module", "工作计划管理");
    }
}
