package com.example.processmgt.controller;

import com.example.common.dto.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    private final ConcurrentHashMap<Long, ProcessDefinition> processMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ProcessInstance> instanceMap = new ConcurrentHashMap<>();
    private final AtomicLong processIdGenerator = new AtomicLong(1);
    private final AtomicLong instanceIdGenerator = new AtomicLong(1);

    @GetMapping("/definition")
    public ResponseResult<List<ProcessDefinition>> listDefinitions(
            @RequestParam(required = false) String status) {
        List<ProcessDefinition> processes = new ArrayList<>(processMap.values());
        return ResponseResult.success(processes);
    }

    @GetMapping("/definition/{id}")
    public ResponseResult<ProcessDefinition> getDefinitionById(@PathVariable Long id) {
        return ResponseResult.success(processMap.get(id));
    }

    @PostMapping("/definition")
    public ResponseResult<ProcessDefinition> saveDefinition(@RequestBody ProcessDefinition process) {
        Long id = processIdGenerator.getAndIncrement();
        process.setId(id);
        process.setProcessNo("PROC-" + String.format("%06d", id));
        process.setStatus("DRAFT");
        process.setCreateTime(LocalDateTime.now());
        process.setUpdateTime(LocalDateTime.now());
        processMap.put(id, process);
        return ResponseResult.success(process);
    }

    @PutMapping("/definition/{id}/publish")
    public ResponseResult<ProcessDefinition> publish(@PathVariable Long id) {
        ProcessDefinition process = processMap.get(id);
        if (process != null) {
            process.setStatus("PUBLISHED");
            process.setUpdateTime(LocalDateTime.now());
            processMap.put(id, process);
        }
        return ResponseResult.success(process);
    }

    @GetMapping("/instance")
    public ResponseResult<List<ProcessInstance>> listInstances(
            @RequestParam(required = false) String processId,
            @RequestParam(required = false) String status) {
        List<ProcessInstance> instances = new ArrayList<>(instanceMap.values());
        if (processId != null) {
            instances = instances.stream().filter(i -> processId.equals(i.getProcessId())).toList();
        }
        return ResponseResult.success(instances);
    }

    @PostMapping("/instance/start")
    public ResponseResult<ProcessInstance> startInstance(@RequestBody StartRequest request) {
        ProcessInstance instance = new ProcessInstance();
        Long id = instanceIdGenerator.getAndIncrement();
        instance.setId(id);
        instance.setProcessId(request.getProcessId());
        instance.setProcessName(request.getProcessName());
        instance.setStatus("RUNNING");
        instance.setCreateTime(LocalDateTime.now());
        instanceMap.put(id, instance);
        return ResponseResult.success(instance);
    }

    @PutMapping("/instance/{id}/complete")
    public ResponseResult<ProcessInstance> completeInstance(@PathVariable Long id) {
        ProcessInstance instance = instanceMap.get(id);
        if (instance != null) {
            instance.setStatus("COMPLETED");
            instance.setEndTime(LocalDateTime.now());
            instanceMap.put(id, instance);
        }
        return ResponseResult.success(instance);
    }

    public static class ProcessDefinition {
        private Long id;
        private String processNo;
        private String name;
        private String description;
        private String status;
        private String category;
        private String xmlContent;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getProcessNo() { return processNo; }
        public void setProcessNo(String processNo) { this.processNo = processNo; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getXmlContent() { return xmlContent; }
        public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        public LocalDateTime getUpdateTime() { return updateTime; }
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    }

    public static class ProcessInstance {
        private Long id;
        private String processId;
        private String processName;
        private String status;
        private String applicant;
        private LocalDateTime createTime;
        private LocalDateTime endTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getProcessId() { return processId; }
        public void setProcessId(String processId) { this.processId = processId; }
        public String getProcessName() { return processName; }
        public void setProcessName(String processName) { this.processName = processName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getApplicant() { return applicant; }
        public void setApplicant(String applicant) { this.applicant = applicant; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }

    public static class StartRequest {
        private String processId;
        private String processName;
        private String applicant;

        public String getProcessId() { return processId; }
        public void setProcessId(String processId) { this.processId = processId; }
        public String getProcessName() { return processName; }
        public void setProcessName(String processName) { this.processName = processName; }
        public String getApplicant() { return applicant; }
        public void setApplicant(String applicant) { this.applicant = applicant; }
    }
}