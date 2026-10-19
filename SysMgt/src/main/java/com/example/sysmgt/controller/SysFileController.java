package com.example.sysmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmgt.entity.SysFile;
import com.example.sysmgt.service.SysFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/file")
public class SysFileController {

    @Autowired
    private SysFileService sysFileService;

    @GetMapping("/page")
    public ResponseResult<Page<SysFile>> page(@RequestParam(defaultValue = "1") int current,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String fileName,
                                              @RequestParam(required = false) String fileType,
                                              @RequestParam(required = false) String storageType,
                                              @RequestParam(required = false) String bizType) {
        return ResponseResult.success(sysFileService.page(current, size, fileName, fileType, storageType, bizType));
    }

    @GetMapping("/list")
    public ResponseResult<List<SysFile>> list(@RequestParam(required = false) String fileName,
                                              @RequestParam(required = false) String fileType,
                                              @RequestParam(required = false) String storageType,
                                              @RequestParam(required = false) String bizType) {
        return ResponseResult.success(sysFileService.list(fileName, fileType, storageType, bizType));
    }

    @GetMapping("/biz/{bizType}/{bizId}")
    public ResponseResult<List<SysFile>> listByBiz(@PathVariable String bizType, @PathVariable String bizId) {
        return ResponseResult.success(sysFileService.listByBiz(bizType, bizId));
    }

    @GetMapping("/{id}")
    public ResponseResult<SysFile> getById(@PathVariable Long id) {
        return ResponseResult.success(sysFileService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody SysFile sysFile) {
        return ResponseResult.success(sysFileService.save(sysFile));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody SysFile sysFile) {
        sysFile.setId(id);
        return ResponseResult.success(sysFileService.updateById(sysFile));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(sysFileService.removeById(id));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return ResponseResult.success(sysFileService.removeByIds(ids));
    }

    @DeleteMapping("/biz/{bizType}/{bizId}")
    public ResponseResult<Boolean> deleteByBiz(@PathVariable String bizType, @PathVariable String bizId) {
        return ResponseResult.success(sysFileService.deleteByBiz(bizType, bizId));
    }
}
