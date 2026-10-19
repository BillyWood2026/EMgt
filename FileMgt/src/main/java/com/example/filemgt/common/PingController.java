package com.example.filemgt.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/file/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "FileMgt", "module", "集中文件管理");
    }
}
