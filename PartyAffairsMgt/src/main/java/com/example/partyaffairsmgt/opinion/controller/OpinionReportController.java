package com.example.partyaffairsmgt.opinion.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.opinion.entity.OpinionReport;
import com.example.partyaffairsmgt.opinion.entity.OpinionReportTemplate;
import com.example.partyaffairsmgt.opinion.service.OpinionReportService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 舆情报告控制器（需求 8.5：日报/周报/月报自动生成、模板配置、闭环率/环比）。 */
@RestController
@RequestMapping("/api/party/opinion/report")
@CrossOrigin(origins = "*")
public class OpinionReportController {

    @Autowired
    private OpinionReportService service;

    @GetMapping("/list")
    public ResponseResult<List<OpinionReport>> list(@RequestParam(required = false) String reportType,
                                                    @RequestParam(required = false) String status) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.list(reportType, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<OpinionReport> detail(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.detail(id));
    }

    /** 自动生成（日报/周报/月报） */
    @PostMapping("/generate")
    public ResponseResult<OpinionReport> generate(@RequestParam String reportType,
                                                  @RequestParam(required = false) Long templateId) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.generate(reportType, templateId));
    }

    @PostMapping
    public ResponseResult<OpinionReport> save(@RequestBody OpinionReport report) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.save(report));
    }

    @PostMapping("/{id}/publish")
    public ResponseResult<OpinionReport> publish(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03);
        return ResponseResult.success(service.publish(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        service.delete(id);
        return ResponseResult.success(null);
    }

    // ---------------- 报告模板 ----------------

    @GetMapping("/template/list")
    public ResponseResult<List<OpinionReportTemplate>> templates() {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.listTemplates());
    }

    @PostMapping("/template")
    public ResponseResult<OpinionReportTemplate> saveTemplate(@RequestBody OpinionReportTemplate t) {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(service.saveTemplate(t));
    }

    @DeleteMapping("/template/{id}")
    public ResponseResult<Void> deleteTemplate(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        service.deleteTemplate(id);
        return ResponseResult.success(null);
    }
}
