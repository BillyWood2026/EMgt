package com.example.culturemgt.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.activity.entity.CulturePosition;
import com.example.culturemgt.activity.mapper.CulturePositionMapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 文化阵地台账：内容更新周期管理，超期未更新由前端按 last_update_date+周期 计算橙警。 */
@Service
public class PositionService {

    @Autowired private CulturePositionMapper mapper;
    @Autowired private AuditService audit;

    public List<CulturePosition> list(String status, String type) {
        LambdaQueryWrapper<CulturePosition> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(CulturePosition::getStatus, status);
        if (type != null && !type.isBlank()) w.eq(CulturePosition::getPosType, type);
        w.orderByAsc(CulturePosition::getId);
        return mapper.selectList(w);
    }

    public CulturePosition save(CulturePosition p) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        int n = ActivityFlow.len(p.getPosName());
        ActivityFlow.require(n >= 2 && n <= 100, "01", "阵地名称需2-100字");
        ActivityFlow.require(p.getUpdateCycleDays() == null || p.getUpdateCycleDays() >= 1, "01", "更新周期须≥1天");
        if (p.getId() == null) {
            p.setStatus(p.getStatus() == null ? "正常" : p.getStatus());
            p.setCreateTime(LocalDateTime.now());
            mapper.insert(p);
            audit.log("NEW", "cul_position", p.getId(), CurrentHeader.operator() + " 新增阵地 " + p.getPosName());
        } else {
            CulturePosition old = mapper.selectById(p.getId());
            ActivityFlow.require(old != null, "01", "阵地不存在");
            p.setStatus(old.getStatus());
            p.setLastUpdateDate(old.getLastUpdateDate());
            p.setUpdateTime(LocalDateTime.now());
            mapper.updateById(p);
            audit.log("EDIT", "cul_position", p.getId(), "调整阵地 " + p.getPosName());
        }
        return p;
    }

    /** 登记一次内容更新：刷新最近更新日期。 */
    public void touch(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CulturePosition p = mapper.selectById(id);
        ActivityFlow.require(p != null, "01", "阵地不存在");
        CulturePosition u = new CulturePosition();
        u.setId(id);
        u.setLastUpdateDate(LocalDate.now());
        u.setUpdateTime(LocalDateTime.now());
        mapper.updateById(u);
        audit.log("TOUCH", "cul_position", id, "阵地内容更新登记：" + p.getPosName());
    }

    /** 正常/停用切换。 */
    public void toggle(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CulturePosition p = mapper.selectById(id);
        ActivityFlow.require(p != null, "01", "阵地不存在");
        CulturePosition u = new CulturePosition();
        u.setId(id);
        u.setStatus("正常".equals(p.getStatus()) ? "停用" : "正常");
        u.setUpdateTime(LocalDateTime.now());
        mapper.updateById(u);
        audit.log("TOGGLE", "cul_position", id, "阵地状态切换为" + u.getStatus());
    }
}
