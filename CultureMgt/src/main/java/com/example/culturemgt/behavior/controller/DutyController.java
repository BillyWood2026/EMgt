package com.example.culturemgt.behavior.controller;

import com.example.culturemgt.behavior.entity.BehaviorDuty;
import com.example.culturemgt.behavior.service.DutyService;
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

/** 干部文化履职填报（季度 5 项）。 */
@RestController
@RequestMapping("/api/culture/behavior/duty")
public class DutyController {

    @Autowired private DutyService service;

    @GetMapping("/mine")
    public ResponseResult<?> mine() {
        return ResponseResult.success(service.mine());
    }

    /** 文化岗待审核列表。 */
    @GetMapping("/todo")
    public ResponseResult<?> todo() {
        return ResponseResult.success(service.todoList());
    }

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String period,
                                  @RequestParam(required = false) String dept) {
        return ResponseResult.success(service.adminList(status, period, dept));
    }

    @GetMapping("/{id}")
    public ResponseResult<?> get(@PathVariable Long id) {
        return ResponseResult.success(service.get(id));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody BehaviorDuty body) {
        body.setId(null);
        return ResponseResult.success(service.save(body));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody BehaviorDuty body) {
        body.setId(id);
        return ResponseResult.success(service.save(body));
    }

    @PostMapping("/{id}/submit")
    public ResponseResult<?> submit(@PathVariable Long id) {
        service.submit(id);
        return ResponseResult.success(null);
    }

    /** 文化岗审核，?pass=&opinion=。 */
    @PostMapping("/{id}/review")
    public ResponseResult<?> review(@PathVariable Long id,
                                    @RequestParam boolean pass,
                                    @RequestParam(required = false) String opinion) {
        service.review(id, pass, opinion);
        return ResponseResult.success(null);
    }
}
