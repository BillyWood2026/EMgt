package com.example.screenmgt.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/screen/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "ScreenMgt", "module", "统一大屏显示中台");
    }
}
