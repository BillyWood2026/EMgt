package com.example.partyaffairsmgt.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.system.entity.SysAuditLog;
import com.example.partyaffairsmgt.system.mapper.SysAuditLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 审计日志（需求 12.5：登录/操作/敏感全留痕，只增不改不删）。
 * E-SY-14 审计日志禁改删；查询仅 R02/R03/R04。
 */
@RestController
@RequestMapping("/api/party/system/audit")
@CrossOrigin(origins = "*")
public class SysAuditController {

    @Autowired
    private SysAuditLogMapper auditLogMapper;

    @GetMapping("/list")
    public ResponseResult<Page<SysAuditLog>> list(@RequestParam(required = false) String opType,
                                                  @RequestParam(required = false) String operator,
                                                  @RequestParam(required = false) String actionLike,
                                                  @RequestParam(required = false) String dateFrom,
                                                  @RequestParam(required = false) String dateTo,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04);
        QueryWrapper<SysAuditLog> qw = new QueryWrapper<>();
        if (opType != null && !opType.isBlank()) qw.eq("op_type", opType);
        if (operator != null && !operator.isBlank()) qw.eq("operator", operator);
        if (actionLike != null && !actionLike.isBlank()) qw.like("action", actionLike);
        if (dateFrom != null && !dateFrom.isBlank()) qw.ge("create_time", dateFrom + " 00:00:00");
        if (dateTo != null && !dateTo.isBlank()) qw.le("create_time", dateTo + " 23:59:59");
        qw.orderByDesc("id");
        return ResponseResult.success(auditLogMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    /** 审计日志禁止修改（E-SY-14）。 */
    @PutMapping("/**")
    public ResponseResult<Boolean> forbidUpdate() {
        throw PartyBizException.block("E-SY-14", "审计日志只增不改，禁止修改操作");
    }

    /** 审计日志禁止删除（E-SY-14）。 */
    @DeleteMapping("/**")
    public ResponseResult<Boolean> forbidDelete() {
        throw PartyBizException.block("E-SY-14", "审计日志只增不删，禁止删除操作");
    }
}
