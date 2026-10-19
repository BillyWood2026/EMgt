package com.example.culturemgt.common.cul;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 审计日志写入。 */
@Service
public class AuditService {

    @Autowired
    private CulAuditLogMapper mapper;

    public void log(String action, String targetType, Object targetId, String detail) {
        CulAuditLog row = new CulAuditLog();
        row.setOperator(CurrentHeader.operator());
        Set<String> roles = CulRole.currentRoles();
        row.setRoleCode(roles.stream().sorted().collect(Collectors.joining(",")));
        row.setAction(action);
        row.setTargetType(targetType);
        row.setTargetId(targetId == null ? null : targetId.toString());
        row.setDetail(detail);
        row.setCreateTime(LocalDateTime.now());
        mapper.insert(row);
    }
}
