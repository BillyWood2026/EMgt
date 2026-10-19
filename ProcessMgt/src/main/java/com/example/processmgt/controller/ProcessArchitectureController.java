package com.example.processmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.processmgt.entity.PolicyDocument;
import com.example.processmgt.entity.ProcessDefinition;
import com.example.processmgt.service.PolicyDocumentService;
import com.example.processmgt.service.ProcessDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程架构聚合端点：已发布流程按层级可见 + 现行制度
 */
@RestController
@RequestMapping("/api/process/architecture")
public class ProcessArchitectureController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private PolicyDocumentService policyDocumentService;

    @GetMapping
    public ResponseResult<Map<String, Object>> architecture() {
        List<ProcessDefinition> activeDefs = processDefinitionService.list(
                new LambdaQueryWrapper<ProcessDefinition>()
                        .eq(ProcessDefinition::getStatus, ProcessDefinition.STATUS_ACTIVE)
                        .orderByAsc(ProcessDefinition::getLevel)
                        .orderByDesc(ProcessDefinition::getUpdateTime));

        Map<String, List<ProcessDefinition>> levels = new HashMap<>();
        levels.put("level3", filterByLevel(activeDefs, ProcessDefinition.LEVEL_3));
        levels.put("level4", filterByLevel(activeDefs, ProcessDefinition.LEVEL_4));
        levels.put("level5", filterByLevel(activeDefs, ProcessDefinition.LEVEL_5));

        List<PolicyDocument> activeDocs = policyDocumentService.list(
                new LambdaQueryWrapper<PolicyDocument>()
                        .eq(PolicyDocument::getStatus, PolicyDocument.STATUS_ACTIVE)
                        .orderByDesc(PolicyDocument::getUpdateTime));

        Map<String, Object> result = new HashMap<>();
        result.put("processes", activeDefs);
        result.put("levels", levels);
        result.put("documents", activeDocs);
        return ResponseResult.success(result);
    }

    private List<ProcessDefinition> filterByLevel(List<ProcessDefinition> all, String level) {
        return all.stream().filter(d -> level.equals(d.getLevel())).toList();
    }
}
