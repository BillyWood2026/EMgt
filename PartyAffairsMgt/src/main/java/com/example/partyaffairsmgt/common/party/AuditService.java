package com.example.partyaffairsmgt.common.party;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 审计日志写入。 */
@Service
public class AuditService {

    @Autowired
    private PartyAuditLogMapper mapper;

    public void log(String action, String targetType, Object targetId, String detail) {
        PartyAuditLog row = new PartyAuditLog();
        row.setOperator(CurrentHeader.operator());
        Set<String> roles = PartyRole.currentRoles();
        String roleCodes = roles.stream().sorted().collect(Collectors.joining(","));
        if (roleCodes != null && roleCodes.length() > 64) roleCodes = roleCodes.substring(0, 64);
        row.setRoleCode(roleCodes);
        row.setAction(action);
        row.setTargetType(targetType);
        row.setTargetId(targetId == null ? null : targetId.toString());
        row.setDetail(detail);
        row.setCreateTime(LocalDateTime.now());
        mapper.insert(row);
    }
}
