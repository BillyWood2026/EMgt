package com.example.culturemgt.activity.controller;

import com.example.culturemgt.activity.entity.CultureActivity;
import com.example.culturemgt.activity.service.ActivityService;
import com.example.culturemgt.common.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/culture/activity")
public class ActivityController {

    @Autowired private ActivityService service;

    /** 前台活动广场：已发布(报名中)+进行中。 */
    @GetMapping("/square")
    public ResponseResult<?> square(@RequestParam(required = false) String type) {
        return ResponseResult.success(service.square(type));
    }

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String keyword) {
        return ResponseResult.success(service.list(status, type, keyword));
    }

    @GetMapping("/{id}")
    public ResponseResult<?> get(@PathVariable Long id) {
        return ResponseResult.success(service.get(id));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody CultureActivity a) {
        a.setId(null);
        return ResponseResult.success(service.save(a));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody CultureActivity a) {
        a.setId(id);
        return ResponseResult.success(service.save(a));
    }

    @PostMapping("/{id}/submit")
    public ResponseResult<?> submit(@PathVariable Long id) {
        service.submit(id);
        return ResponseResult.success(true);
    }

    @PostMapping("/{id}/approve")
    public ResponseResult<?> approve(@PathVariable Long id,
                                     @RequestParam boolean pass,
                                     @RequestParam(required = false) String opinion) {
        service.approve(id, pass, opinion);
        return ResponseResult.success(true);
    }

    /** 已发布 → 进行中。 */
    @PostMapping("/{id}/start")
    public ResponseResult<?> start(@PathVariable Long id) {
        service.start(id);
        return ResponseResult.success(true);
    }

    /** 进行中 → 待复盘（结束活动归档素材）。 */
    @PostMapping("/{id}/finish")
    public ResponseResult<?> finish(@PathVariable Long id) {
        service.toReview(id);
        return ResponseResult.success(true);
    }

    /** 待复盘 → 已闭环：body 复盘总结/成效评估/素材JSON数组。 */
    @PostMapping("/{id}/close")
    public ResponseResult<?> close(@PathVariable Long id, @RequestBody CultureActivity body) {
        service.close(id, body.getSummary(), body.getResultEval(), body.getArchiveFiles());
        return ResponseResult.success(true);
    }

    /** 已发布 → 已取消。 */
    @PostMapping("/{id}/cancel")
    public ResponseResult<?> cancel(@PathVariable Long id, @RequestParam String reason) {
        service.cancel(id, reason);
        return ResponseResult.success(true);
    }
}
