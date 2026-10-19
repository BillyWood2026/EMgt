package com.example.iotmgt.iot.controller;

import com.example.iotmgt.common.ResponseResult;
import com.example.iotmgt.iot.entity.IotRule;
import com.example.iotmgt.iot.mapper.IotRuleMapper;
import com.example.iotmgt.iot.service.IotService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/iot/rule")
public class IotRuleController {

    private final IotService iotService;
    private final IotRuleMapper ruleMapper;

    public IotRuleController(IotService iotService, IotRuleMapper ruleMapper) {
        this.iotService = iotService;
        this.ruleMapper = ruleMapper;
    }

    @GetMapping("/list")
    public ResponseResult<List<IotRule>> list(@RequestParam(required = false) String status) {
        return ResponseResult.ok(iotService.listRules(status));
    }

    @GetMapping("/{id}")
    public ResponseResult<IotRule> getById(@PathVariable Long id) {
        return ResponseResult.ok(ruleMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody IotRule rule) {
        rule.setId(null);
        rule.setCreateTime(LocalDateTime.now());
        if (rule.getStatus() == null) rule.setStatus("启用");
        return ResponseResult.ok(ruleMapper.insert(rule) > 0);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody IotRule rule) {
        rule.setId(id);
        rule.setUpdateTime(LocalDateTime.now());
        return ResponseResult.ok(ruleMapper.updateById(rule) > 0);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(ruleMapper.deleteById(id) > 0);
    }
}
