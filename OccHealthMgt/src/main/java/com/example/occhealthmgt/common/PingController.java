package com.example.occhealthmgt.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/eehs/health/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "OccHealthMgt", "module", "职业健康管理");
    }
}
