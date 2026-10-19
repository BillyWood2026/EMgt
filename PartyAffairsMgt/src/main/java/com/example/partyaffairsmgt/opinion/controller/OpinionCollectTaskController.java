package com.example.partyaffairsmgt.opinion.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.opinion.entity.OpinionCollectLog;
import com.example.partyaffairsmgt.opinion.entity.OpinionCollectTask;
import com.example.partyaffairsmgt.opinion.service.OpinionCollectService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 采集任务与日志控制器（需求 8.1：定时采集/指纹去重/重试/配额/风暴）。 */
@RestController
@RequestMapping("/api/party/opinion/task")
@CrossOrigin(origins = "*")
public class OpinionCollectTaskController {

    @Autowired
    private OpinionCollectService service;

    @GetMapping("/list")
    public ResponseResult<List<OpinionCollectTask>> list(@RequestParam(required = false) String status) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.listTasks(status));
    }

    @PostMapping
    public ResponseResult<OpinionCollectTask> save(@RequestBody OpinionCollectTask task) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.saveTask(task));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        service.deleteTask(id);
        return ResponseResult.success(null);
    }

    /** 执行采集（simulate/fail 为演示参数：风暴、配额、失败重试） */
    @PostMapping("/{id}/run")
    public ResponseResult<Map<String, Object>> run(@PathVariable Long id,
                                                   @RequestParam(required = false) Integer simulate,
                                                   @RequestParam(defaultValue = "false") boolean fail) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.run(id, simulate, fail));
    }

    @GetMapping("/log/list")
    public ResponseResult<List<OpinionCollectLog>> logs(@RequestParam(required = false) Long taskId,
                                                        @RequestParam(required = false) String status) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.listLogs(taskId, status));
    }
}
