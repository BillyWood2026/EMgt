package com.example.messagemgt.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/message/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "MessageMgt", "module", "统一消息中心");
    }
}
