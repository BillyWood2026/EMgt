package com.example.filemgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.filemgt.common.ResponseResult;
import com.example.filemgt.entity.BizAttachment;
import com.example.filemgt.service.BizAttachmentService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/file/attachment")
public class BizAttachmentController {

    private final BizAttachmentService bizAttachmentService;

    public BizAttachmentController(BizAttachmentService bizAttachmentService) {
        this.bizAttachmentService = bizAttachmentService;
    }

    @GetMapping("/list")
    public ResponseResult<List<BizAttachment>> list(@RequestParam(required = false) String serviceCode,
                                                    @RequestParam(required = false) String businessBillId) {
        LambdaQueryWrapper<BizAttachment> w = new LambdaQueryWrapper<>();
        if (serviceCode != null && !serviceCode.isEmpty()) {
            w.eq(BizAttachment::getServiceCode, serviceCode);
        }
        if (businessBillId != null && !businessBillId.isEmpty()) {
            w.eq(BizAttachment::getBusinessBillId, businessBillId);
        }
        w.orderByDesc(BizAttachment::getId);
        return ResponseResult.ok(bizAttachmentService.list(w));
    }

    @GetMapping("/{id}")
    public ResponseResult<BizAttachment> getById(@PathVariable Long id) {
        return ResponseResult.ok(bizAttachmentService.getById(id));
    }

    @PostMapping
    public ResponseResult<BizAttachment> create(@RequestBody BizAttachment entity) {
        entity.setId(null);
        if (entity.getReadOnly() == null) {
            entity.setReadOnly(0);
        }
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        bizAttachmentService.save(entity);
        return ResponseResult.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<BizAttachment> update(@PathVariable Long id, @RequestBody BizAttachment entity) {
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        bizAttachmentService.updateById(entity);
        return ResponseResult.ok(bizAttachmentService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(bizAttachmentService.removeById(id));
    }
}
