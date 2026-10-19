package com.example.talentdev.controller;

import com.example.common.dto.ResponseResult;
import com.example.talentdev.entity.DevTracking;
import com.example.talentdev.service.DevTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dev Tracking Controller (进度跟踪)
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/talent/tracking")
public class DevTrackingController {

    @Autowired
    private DevTrackingService devTrackingService;

    @GetMapping("/list")
    public ResponseResult<List<DevTracking>> list() {
        return ResponseResult.success(devTrackingService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<DevTracking> getById(@PathVariable Long id) {
        return ResponseResult.success(devTrackingService.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody DevTracking devTracking) {
        devTrackingService.save(devTracking);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody DevTracking devTracking) {
        devTracking.setId(id);
        devTrackingService.updateById(devTracking);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        devTrackingService.removeById(id);
        return ResponseResult.success();
    }
}
