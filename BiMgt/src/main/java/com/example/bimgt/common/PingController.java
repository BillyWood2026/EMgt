package com.example.bimgt.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/bi/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "BiMgt", "module", "统一报表及BI中台");
    }
}
