package com.example.partyaffairsmgt.system.service;

import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.system.entity.SysAuditLog;
import com.example.partyaffairsmgt.system.mapper.SysAuditLogMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 审计日志写入器（需求 12.5）：所有敏感操作统一留痕。 */
@Service
public class SysAuditWriter {

    @Autowired
    private SysAuditLogMapper auditLogMapper;

    public void write(String opType, String action, String targetType, Object targetId,
                      String beforeValue, String afterValue, String detail) {
        SysAuditLog log = new SysAuditLog();
        log.setOperator(CurrentHeader.operator());
        List<String> roles = new ArrayList<>(PartyRole.currentRoles());
        Collections.sort(roles);
        log.setRoleCode(String.join(",", roles));
        log.setOpType(opType);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId == null ? null : String.valueOf(targetId));
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        log.setIp(attr == null ? null : attr.getRequest().getRemoteAddr());
        log.setBeforeValue(beforeValue);
        log.setAfterValue(afterValue);
        log.setDetail(detail);
        log.setCreateTime(LocalDateTime.now());
        auditLogMapper.insert(log);
    }
}
