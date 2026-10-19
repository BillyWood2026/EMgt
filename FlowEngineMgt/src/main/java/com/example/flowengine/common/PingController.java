package com.example.flowengine.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/flow/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok", "service", "FlowEngineMgt", "module", "统一流程引擎BPMN");
    }
}
