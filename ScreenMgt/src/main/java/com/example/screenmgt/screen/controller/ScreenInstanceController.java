package com.example.screenmgt.screen.controller;

import com.example.screenmgt.common.ResponseResult;
import com.example.screenmgt.screen.entity.ScreenInstance;
import com.example.screenmgt.screen.service.ScreenInstanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/screen/instance")
public class ScreenInstanceController {

    private final ScreenInstanceService service;

    public ScreenInstanceController(ScreenInstanceService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseResult<List<ScreenInstance>> list() {
        return ResponseResult.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<ScreenInstance> get(@PathVariable Long id) {
        return ResponseResult.ok(service.getById(id));
    }

    @PostMapping
    public ResponseResult<ScreenInstance> save(@RequestBody ScreenInstance s) {
        return ResponseResult.ok(service.upsert(s));
    }

    @PutMapping("/{id}")
    public ResponseResult<ScreenInstance> update(@PathVariable Long id, @RequestBody ScreenInstance s) {
        s.setId(id);
        return ResponseResult.ok(service.upsert(s));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(service.removeById(id));
    }

    /** 发布大屏并生成访问令牌 */
    @PostMapping("/{id}/publish")
    public ResponseResult<ScreenInstance> publish(@PathVariable Long id) {
        return ResponseResult.ok(service.publish(id));
    }

    /** 大屏渲染: 完整配置+组件数据(模拟) */
    @GetMapping("/{id}/render")
    public ResponseResult<Map<String, Object>> render(@PathVariable Long id) {
        return ResponseResult.ok(service.render(id));
    }
}
