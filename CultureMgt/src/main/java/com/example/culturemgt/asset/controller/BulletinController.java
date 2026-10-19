package com.example.culturemgt.asset.controller;

import com.example.culturemgt.asset.entity.CultureBulletin;
import com.example.culturemgt.asset.service.BulletinService;
import com.example.culturemgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/culture/bulletin")
public class BulletinController {

    private final BulletinService bulletinService;

    public BulletinController(BulletinService bulletinService) {
        this.bulletinService = bulletinService;
    }

    @GetMapping("/list")
    public ResponseResult<List<CultureBulletin>> list(@RequestParam(required = false) String status,
                                                      @RequestParam(required = false) String keyword) {
        return ResponseResult.success(bulletinService.list(status, keyword));
    }

    @GetMapping("/public")
    public ResponseResult<List<CultureBulletin>> publicList() {
        return ResponseResult.success(bulletinService.publicList());
    }

    @GetMapping("/{id}")
    public ResponseResult<CultureBulletin> get(@PathVariable Long id) {
        return ResponseResult.success(bulletinService.get(id));
    }

    @PostMapping
    public ResponseResult<CultureBulletin> save(@RequestBody CultureBulletin bulletin) {
        return ResponseResult.success(bulletinService.save(bulletin));
    }

    @PostMapping("/{id}/publish")
    public ResponseResult<Void> publish(@PathVariable Long id) {
        bulletinService.publish(id);
        return ResponseResult.success(null);
    }

    @PostMapping("/{id}/offline")
    public ResponseResult<Void> offline(@PathVariable Long id) {
        bulletinService.offline(id);
        return ResponseResult.success(null);
    }

    @PostMapping("/{id}/view")
    public ResponseResult<CultureBulletin> view(@PathVariable Long id) {
        return ResponseResult.success(bulletinService.view(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        bulletinService.delete(id);
        return ResponseResult.success(null);
    }
}
