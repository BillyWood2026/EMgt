package com.example.iotmgt.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/iot/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "IotMgt", "module", "统一物联网IOT中台");
    }
}
