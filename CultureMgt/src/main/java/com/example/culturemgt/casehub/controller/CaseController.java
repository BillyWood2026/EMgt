package com.example.culturemgt.casehub.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.casehub.entity.CaseItem;
import com.example.culturemgt.casehub.service.CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/culture/case")
public class CaseController {

    @Autowired private CaseService service;

    /** 案例广场（前台）。普通员工仅看公开案例；管理岗带 publicOnly=false 可含内部反面案例。 */
    @GetMapping("/square")
    public ResponseResult<?> square(@RequestParam(required = false) String type,
                                    @RequestParam(required = false) String tag,
                                    @RequestParam(defaultValue = "true") boolean publicOnly) {
        return ResponseResult.success(service.square(type, tag, publicOnly));
    }

    @GetMapping("/mine")
    public ResponseResult<?> mine() {
        return ResponseResult.success(service.mine());
    }

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String keyword) {
        return ResponseResult.success(service.adminList(status, type, keyword));
    }

    @GetMapping("/{id}")
    public ResponseResult<?> get(@PathVariable Long id) {
        return ResponseResult.success(service.get(id));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody CaseItem c) {
        c.setId(null);
        return ResponseResult.success(service.save(c));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody CaseItem c) {
        c.setId(id);
        return ResponseResult.success(service.save(c));
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

    @PostMapping("/{id}/toggle-archive")
    public ResponseResult<?> toggleArchive(@PathVariable Long id) {
        service.toggleArchive(id);
        return ResponseResult.success(true);
    }

    /** 供新闻宣传(s9)/培训(s5)模块调用的引用加分。 */
    @PostMapping("/{id}/cite")
    public ResponseResult<?> cite(@PathVariable Long id, @RequestParam String type) {
        service.cite(id, type);
        return ResponseResult.success(true);
    }
}
