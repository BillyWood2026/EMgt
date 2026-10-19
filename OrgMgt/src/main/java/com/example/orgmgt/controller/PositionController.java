package com.example.orgmgt.controller;

import com.example.common.dto.ResponseResult;
import com.example.orgmgt.entity.Position;
import com.example.orgmgt.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/position")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @GetMapping({"", "/list"})
    public ResponseResult<List<Position>> list(@RequestParam(required = false) Long orgId,
                                               @RequestParam(required = false) String orgCode,
                                               @RequestParam(required = false) String positionLevel,
                                               @RequestParam(required = false) String positionStatus) {
        List<Position> result;
        if (orgId != null) {
            result = positionService.findByOrgId(orgId);
        } else if (orgCode != null) {
            result = positionService.findByOrgCode(orgCode);
        } else if (positionLevel != null) {
            result = positionService.findByPositionLevel(positionLevel);
        } else if (positionStatus != null) {
            result = positionService.findByPositionStatus(positionStatus);
        } else {
            result = positionService.findAll();
        }
        return ResponseResult.success(result);
    }

    @GetMapping("/{id}")
    public ResponseResult<Position> getById(@PathVariable Long id) {
        return ResponseResult.success(positionService.findById(id));
    }

    @GetMapping("/code/{positionCode}")
    public ResponseResult<Position> getByPositionCode(@PathVariable String positionCode) {
        return ResponseResult.success(positionService.findByPositionCode(positionCode));
    }

    @GetMapping("/vacant")
    public ResponseResult<List<Position>> getVacantPositions() {
        return ResponseResult.success(positionService.findVacantPositions());
    }

    @PostMapping
    public ResponseResult<Position> create(@RequestBody Position position) {
        return ResponseResult.success(positionService.create(position));
    }

    @PutMapping("/{id}")
    public ResponseResult<Position> update(@PathVariable Long id, @RequestBody Position position) {
        position.setId(id);
        return ResponseResult.success(positionService.update(position));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        positionService.deleteById(id);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/status")
    public ResponseResult<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        positionService.updateStatus(id, status);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/count")
    public ResponseResult<Void> updateCurrentCount(@PathVariable Long id, @RequestParam Integer count) {
        positionService.updateCurrentCount(id, count);
        return ResponseResult.success();
    }

    @PostMapping("/batch")
    public ResponseResult<List<Position>> batchCreate(@RequestBody List<Position> positions) {
        return ResponseResult.success(positionService.batchCreate(positions));
    }

    @DeleteMapping("/batch")
    public ResponseResult<Void> batchDelete(@RequestBody List<Long> ids) {
        positionService.batchDelete(ids);
        return ResponseResult.success();
    }

    @PutMapping("/batch/org")
    public ResponseResult<Void> batchUpdateOrg(@RequestBody List<Long> positionIds, @RequestParam Long newOrgId) {
        positionService.batchUpdateOrg(positionIds, newOrgId);
        return ResponseResult.success();
    }
}