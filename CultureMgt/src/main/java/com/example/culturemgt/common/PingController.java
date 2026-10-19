package com.example.culturemgt.common;

import com.example.culturemgt.common.cul.CulRole;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 框架探活与当前角色识别。 */
@RestController
@RequestMapping("/api/culture")
public class PingController {

    @GetMapping("/ping")
    public ResponseResult<Map<String, Object>> ping() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("service", "CultureMgt");
        data.put("version", "1.0.0");
        data.put("status", "UP");
        data.put("time", LocalDateTime.now().toString());
        data.put("roles", CulRole.currentRoles());
        return ResponseResult.success(data);
    }
}
