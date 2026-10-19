package com.example.culturemgt.foundation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.foundation.entity.Milestone;
import com.example.culturemgt.foundation.mapper.MilestoneMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 文化里程碑：文化岗维护，支持前台展示开关与归档，禁物理删除。 */
@Service
public class MilestoneService {

    @Autowired private MilestoneMapper mapper;
    @Autowired private AuditService audit;

    public List<Milestone> list(String status, String front) {
        LambdaQueryWrapper<Milestone> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(Milestone::getStatus, status);
        if ("1".equals(front)) w.eq(Milestone::getShowFront, 1);
        w.orderByAsc(Milestone::getOccurDate).orderByDesc(Milestone::getId);
        return mapper.selectList(w);
    }

    public Milestone save(Milestone m) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        FoundationFlow.require(m.getOccurDate() != null, "01", "事件发生时间必填");
        FoundationFlow.require(!m.getOccurDate().isAfter(LocalDate.now()), "01", "里程碑事件时间不可为未来时间");
        FoundationFlow.requireText(m.getTitle(), 2, 120, "事件标题", "01");
        FoundationFlow.requireText(m.getContent(), 5, 2000, "事件描述", "01");
        if (m.getShowFront() == null) m.setShowFront(1);
        if (m.getId() == null) {
            m.setStatus("启用");
            m.setCreator(CurrentHeader.operator());
            m.setCreateTime(LocalDateTime.now());
            mapper.insert(m);
            audit.log("NEW", "cul_milestone", m.getId(), "新增里程碑 " + m.getTitle());
        } else {
            Milestone old = mapper.selectById(m.getId());
            FoundationFlow.require(old != null, "01", "里程碑不存在");
            FoundationFlow.require("启用".equals(old.getStatus()), "06", "仅启用里程碑可编辑，归档请先恢复");
            m.setStatus(old.getStatus());
            m.setUpdateTime(LocalDateTime.now());
            mapper.updateById(m);
        }
        return m;
    }

    /** 启用/归档/前台开关。 */
    public void toggle(Long id, String action) {
        Milestone m = mapper.selectById(id);
        FoundationFlow.require(m != null, "01", "里程碑不存在");
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        switch (action) {
            case "archive" -> {
                FoundationFlow.require("启用".equals(m.getStatus()), "03", "仅启用可归档");
                m.setStatus("归档");
            }
            case "enable" -> {
                FoundationFlow.require("归档".equals(m.getStatus()), "03", "仅归档可恢复启用");
                m.setStatus("启用");
            }
            case "frontOn" -> m.setShowFront(1);
            case "frontOff" -> m.setShowFront(0);
            default -> FoundationFlow.require(false, "03", "未知操作：" + action);
        }
        m.setUpdateTime(LocalDateTime.now());
        mapper.updateById(m);
        audit.log("TOGGLE", "cul_milestone", id, "操作：" + action);
    }
}
