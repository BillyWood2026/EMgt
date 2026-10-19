package com.example.environmgt.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/eehs/env/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "EnvironMgt", "module", "环境管理");
    }
}
