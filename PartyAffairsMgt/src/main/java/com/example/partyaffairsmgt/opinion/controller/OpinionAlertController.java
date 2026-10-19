package com.example.partyaffairsmgt.opinion.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.opinion.entity.OpinionAlert;
import com.example.partyaffairsmgt.opinion.entity.OpinionAlertNotify;
import com.example.partyaffairsmgt.opinion.entity.OpinionAlertRule;
import com.example.partyaffairsmgt.opinion.service.OpinionAlertService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 舆情预警分级控制器（需求 8.3：四色矩阵/静默时段/推送失败/合并/认领升级）。 */
@RestController
@RequestMapping("/api/party/opinion/alert")
@CrossOrigin(origins = "*")
public class OpinionAlertController {

    @Autowired
    private OpinionAlertService service;

    @GetMapping("/list")
    public ResponseResult<List<OpinionAlert>> list(@RequestParam(required = false) String level,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String keyword) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.list(level, status, keyword));
    }

    @GetMapping("/{id}")
    public ResponseResult<OpinionAlert> detail(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.detail(id));
    }

    /** 按阈值矩阵触发预警（failNotify 演示推送失败重试记录） */
    @PostMapping("/trigger/{analysisId}")
    public ResponseResult<OpinionAlert> trigger(@PathVariable Long analysisId,
                                                @RequestParam(defaultValue = "false") boolean failNotify) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.trigger(analysisId, failNotify));
    }

    @PostMapping("/{id}/claim")
    public ResponseResult<OpinionAlert> claim(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.claim(id));
    }

    /** 红色预警超时未认领升级（force 用于演示） */
    @PostMapping("/{id}/escalate")
    public ResponseResult<OpinionAlert> escalate(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "false") boolean force) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.escalateUnclaimed(id, force));
    }

    // ---------------- 预警规则配置 ----------------

    @GetMapping("/rule/list")
    public ResponseResult<List<OpinionAlertRule>> rules() {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.listRules());
    }

    @PostMapping("/rule")
    public ResponseResult<OpinionAlertRule> saveRule(@RequestBody OpinionAlertRule rule) {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(service.saveRule(rule));
    }

    @DeleteMapping("/rule/{id}")
    public ResponseResult<Void> deleteRule(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        service.deleteRule(id);
        return ResponseResult.success(null);
    }

    // ---------------- 预警通知设置 ----------------

    @GetMapping("/notify/list")
    public ResponseResult<List<OpinionAlertNotify>> notifyList() {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.listNotify());
    }

    @PostMapping("/notify")
    public ResponseResult<OpinionAlertNotify> saveNotify(@RequestBody OpinionAlertNotify n) {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(service.saveNotify(n));
    }

    @DeleteMapping("/notify/{id}")
    public ResponseResult<Void> deleteNotify(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        service.deleteNotify(id);
        return ResponseResult.success(null);
    }
}
