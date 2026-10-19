package com.example.skillcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillcert.entity.SkillWarning;
import com.example.skillcert.mapper.SkillWarningMapper;
import com.example.skillcert.service.SkillWarningService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class SkillWarningServiceImpl extends ServiceImpl<SkillWarningMapper, SkillWarning>
        implements SkillWarningService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public IPage<SkillWarning> pageWarnings(long current, long size, String warningType,
                                            String orgId, String skillTypeId, String keyword) {
        LambdaQueryWrapper<SkillWarning> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(warningType), SkillWarning::getWarningType, warningType)
                .eq(StringUtils.hasText(orgId), SkillWarning::getOrgName, orgId)
                .eq(StringUtils.hasText(skillTypeId), SkillWarning::getSkillTypeId, skillTypeId)
                .and(StringUtils.hasText(keyword), w -> w
                .like(SkillWarning::getUserName, keyword)
                .or().like(SkillWarning::getStaffNo, keyword))
                .orderByDesc(SkillWarning::getId);
        IPage<SkillWarning> page = this.page(new Page<>(current, size), wrapper);
        for (SkillWarning w : page.getRecords()) {
            w.setHandleStatusText(handleStatusText(w.getHandleStatus()));
        }
        return page;
    }

    @Override
    public boolean process(Long id, Map<String, Object> body) {
        SkillWarning w = this.getById(id);
        if (w == null) {
            return false;
        }
        Object processType = body.get("processType");
        Object processRemark = body.get("processRemark");
        if (processType != null) {
            w.setProcessType(String.valueOf(processType));
        }
        if (processRemark != null) {
            w.setProcessRemark(String.valueOf(processRemark));
        }
        w.setHandleStatus("PROCESSED");
        if ("EXEMPT".equals(w.getProcessType())) {
            w.setHandleStatus("EXEMPTED");
        }
        w.setHandlerName("admin");
        w.setHandleTime(LocalDateTime.now().format(FMT));
        return this.updateById(w);
    }

    private String handleStatusText(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "PENDING": return "待处理";
            case "PROCESSING": return "处理中";
            case "PROCESSED": return "已处理";
            case "EXEMPTED": return "已豁免";
            default: return status;
        }
    }
}
