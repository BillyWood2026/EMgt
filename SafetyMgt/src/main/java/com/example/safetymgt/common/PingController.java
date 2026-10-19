package com.example.safetymgt.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/eehs/safety/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "SafetyMgt", "module", "安全管理");
    }
}
