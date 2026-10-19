package com.example.sysmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmgt.entity.SysNotice;
import com.example.sysmgt.service.SysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/notice")
public class SysNoticeController {

    @Autowired
    private SysNoticeService sysNoticeService;

    @GetMapping("/page")
    public ResponseResult<Page<SysNotice>> page(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String noticeTitle,
                                                @RequestParam(required = false) String noticeType,
                                                @RequestParam(required = false) String status) {
        return ResponseResult.success(sysNoticeService.page(current, size, noticeTitle, noticeType, status));
    }

    @GetMapping("/list")
    public ResponseResult<List<SysNotice>> list(@RequestParam(required = false) String noticeTitle,
                                                @RequestParam(required = false) String noticeType,
                                                @RequestParam(required = false) String status) {
        return ResponseResult.success(sysNoticeService.list(noticeTitle, noticeType, status));
    }

    @GetMapping("/published")
    public ResponseResult<List<SysNotice>> publishedList(@RequestParam(required = false) String noticeType) {
        return ResponseResult.success(sysNoticeService.listPublished(noticeType));
    }

    @GetMapping("/{id}")
    public ResponseResult<SysNotice> getById(@PathVariable Long id) {
        return ResponseResult.success(sysNoticeService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody SysNotice sysNotice) {
        return ResponseResult.success(sysNoticeService.save(sysNotice));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody SysNotice sysNotice) {
        sysNotice.setId(id);
        return ResponseResult.success(sysNoticeService.updateById(sysNotice));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(sysNoticeService.removeById(id));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return ResponseResult.success(sysNoticeService.removeByIds(ids));
    }

    @PutMapping("/{id}/publish")
    public ResponseResult<Boolean> publish(@PathVariable Long id) {
        return ResponseResult.success(sysNoticeService.publish(id));
    }

    @PutMapping("/{id}/offline")
    public ResponseResult<Boolean> offline(@PathVariable Long id) {
        return ResponseResult.success(sysNoticeService.offline(id));
    }
}
