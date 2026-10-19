package com.example.partyaffairsmgt.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.system.entity.SysOrgPosition;
import com.example.partyaffairsmgt.system.mapper.SysOrgPositionMapper;
import com.example.partyaffairsmgt.system.service.SysAuditWriter;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 组织岗位任职管理（需求 12.2：党务职务与组织/党员关联，审批路由依据）。
 * E-SY-08 同组织同职务已有在任且非兼职；E-SY-16 必填缺失。
 */
@RestController
@RequestMapping("/api/party/system/position")
@CrossOrigin(origins = "*")
public class SysPositionController {

    @Autowired
    private SysOrgPositionMapper positionMapper;
    @Autowired
    private SysAuditWriter auditWriter;

    @GetMapping("/list")
    public ResponseResult<Page<SysOrgPosition>> list(@RequestParam(required = false) Long orgId,
                                                     @RequestParam(required = false) String positionName,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        PartyRole.requireAny(PartyRole.READ);
        QueryWrapper<SysOrgPosition> qw = new QueryWrapper<>();
        if (orgId != null) qw.eq("org_id", orgId);
        if (positionName != null && !positionName.isBlank()) qw.eq("position_name", positionName);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        qw.orderByDesc("id");
        return ResponseResult.success(positionMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @PostMapping
    public ResponseResult<SysOrgPosition> create(@RequestBody SysOrgPosition pos) {
        PartyRole.requireAny(PartyRole.R02);
        checkConflict(pos, null);
        LocalDateTime now = LocalDateTime.now();
        pos.setId(null);
        if (pos.getStatus() == null || pos.getStatus().isBlank()) pos.setStatus("在任");
        if (pos.getPartTime() == null) pos.setPartTime(0);
        pos.setCreateTime(now);
        pos.setUpdateTime(now);
        positionMapper.insert(pos);
        auditWriter.write("操作", "任职登记", "sys_org_position", pos.getId(), null,
                pos.getOrgName() + "/" + pos.getPositionName() + "/" + pos.getMemberName(), null);
        return ResponseResult.success(pos);
    }

    @PutMapping("/{id}")
    public ResponseResult<SysOrgPosition> update(@PathVariable Long id, @RequestBody SysOrgPosition pos) {
        PartyRole.requireAny(PartyRole.R02);
        SysOrgPosition old = positionMapper.selectById(id);
        if (old == null) throw PartyBizException.block("E-SY-16", "任职记录不存在：" + id);
        pos.setId(id);
        checkConflict(pos, id);
        pos.setCreateTime(null);
        pos.setUpdateTime(LocalDateTime.now());
        positionMapper.updateById(pos);
        auditWriter.write("操作", "任职变动", "sys_org_position", id,
                old.getStatus() + "/" + old.getMemberName(), pos.getStatus() + "/" + pos.getMemberName(), null);
        return ResponseResult.success(positionMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        SysOrgPosition old = positionMapper.selectById(id);
        if (old == null) throw PartyBizException.block("E-SY-16", "任职记录不存在：" + id);
        boolean ok = positionMapper.deleteById(id) > 0;
        auditWriter.write("操作", "任职删除", "sys_org_position", id,
                old.getOrgName() + "/" + old.getPositionName(), null, null);
        return ResponseResult.success(ok);
    }

    /** E-SY-08：同组织同职务已有"在任"且双方均非兼职时阻断。 */
    private void checkConflict(SysOrgPosition pos, Long excludeId) {
        if (pos.getOrgId() == null || pos.getPositionName() == null || pos.getPositionName().isBlank()) {
            throw PartyBizException.block("E-SY-16", "必填项缺失：组织/职务");
        }
        String status = pos.getStatus() == null || pos.getStatus().isBlank() ? "在任" : pos.getStatus();
        boolean fullTime = (pos.getPartTime() == null || pos.getPartTime() == 0);
        if (!"在任".equals(status) || !fullTime) return;
        QueryWrapper<SysOrgPosition> qw = new QueryWrapper<>();
        qw.eq("org_id", pos.getOrgId())
                .eq("position_name", pos.getPositionName())
                .eq("status", "在任")
                .eq("part_time", 0);
        if (excludeId != null) qw.ne("id", excludeId);
        if (positionMapper.selectCount(qw) > 0) {
            throw PartyBizException.block("E-SY-08",
                    "该组织已存在在任的" + pos.getPositionName() + "（非兼职），请先办理离任");
        }
    }
}
