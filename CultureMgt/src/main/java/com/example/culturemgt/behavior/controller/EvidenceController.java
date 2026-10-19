package com.example.culturemgt.behavior.controller;

import com.example.culturemgt.behavior.entity.BehaviorEvidence;
import com.example.culturemgt.behavior.service.EvidenceService;
import com.example.culturemgt.common.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 个人行为举证（前台 + 部门初审 + 文化复核）。 */
@RestController
@RequestMapping("/api/culture/behavior/evidence")
public class EvidenceController {

    @Autowired private EvidenceService service;

    /** 我的举证列表。 */
    @GetMapping("/mine")
    public ResponseResult<?> mine() {
        return ResponseResult.success(service.mine());
    }

    /** 待办列表：部门负责人看待初审(stage=1)，文化岗看待复核(stage=2)。 */
    @GetMapping("/todo")
    public ResponseResult<?> todo() {
        return ResponseResult.success(service.todoList());
    }

    /** 后台全量列表（文化岗）。 */
    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String dimension,
                                  @RequestParam(required = false) String keyword) {
        return ResponseResult.success(service.adminList(status, dimension, keyword));
    }

    @GetMapping("/{id}")
    public ResponseResult<?> get(@PathVariable Long id) {
        return ResponseResult.success(service.get(id));
    }

    /** 新建草稿。 */
    @PostMapping
    public ResponseResult<?> create(@RequestBody BehaviorEvidence body) {
        body.setId(null);
        return ResponseResult.success(service.save(body));
    }

    /** 编辑草稿（仅本人草稿可编辑）。 */
    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody BehaviorEvidence body) {
        body.setId(id);
        return ResponseResult.success(service.save(body));
    }

    /** 提交（草稿/已驳回 → 待初审）。 */
    @PostMapping("/{id}/submit")
    public ResponseResult<?> submit(@PathVariable Long id) {
        service.submit(id);
        return ResponseResult.success(null);
    }

    /** 审批（stage=1 部门初审 / stage=2 文化复核）。 */
    @PostMapping("/{id}/approve")
    public ResponseResult<?> approve(@PathVariable Long id,
                                     @RequestParam boolean pass,
                                     @RequestParam(required = false) String opinion) {
        return ResponseResult.success(service.approve(id, pass, opinion));
    }
}
