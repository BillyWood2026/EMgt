package com.example.energymgt.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/eehs/energy/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "EnergyMgt", "module", "能源管理");
    }
}
