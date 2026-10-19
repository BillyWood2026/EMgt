package com.example.culturemgt.casehub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.casehub.entity.CaseTag;
import com.example.culturemgt.casehub.mapper.CaseTagMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 案例标签与激励积分配置。 */
@Service
public class CaseTagService {

    @Autowired private CaseTagMapper mapper;
    @Autowired private AuditService audit;

    public List<CaseTag> list(String status) {
        LambdaQueryWrapper<CaseTag> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(CaseTag::getStatus, status);
        w.orderByAsc(CaseTag::getId);
        return mapper.selectList(w);
    }

    public CaseTag save(CaseTag t) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CaseFlow.require(CaseFlow.len(t.getTagName()) >= 2 && CaseFlow.len(t.getTagName()) <= 64, "01", "标签名称需2-64字");
        if (t.getScorePositive() == null || t.getScorePositive() < 0) t.setScorePositive(10);
        if (t.getScoreNegative() == null || t.getScoreNegative() < 0) t.setScoreNegative(5);
        if (t.getScoreNews() == null || t.getScoreNews() < 0) t.setScoreNews(5);
        if (t.getScoreTraining() == null || t.getScoreTraining() < 0) t.setScoreTraining(3);
        if (t.getId() == null) {
            Long dup = mapper.selectCount(new LambdaQueryWrapper<CaseTag>().eq(CaseTag::getTagName, t.getTagName()));
            CaseFlow.require(dup == 0, "01", "标签「" + t.getTagName() + "」已存在");
            t.setStatus("启用");
            t.setCreator(CurrentHeader.operator());
            t.setCreateTime(LocalDateTime.now());
            mapper.insert(t);
            audit.log("NEW", "cul_case_tag", t.getId(), "新增案例标签 " + t.getTagName());
        } else {
            CaseTag old = mapper.selectById(t.getId());
            CaseFlow.require(old != null, "01", "标签不存在");
            t.setTagName(old.getTagName());
            t.setStatus(old.getStatus());
            t.setUpdateTime(LocalDateTime.now());
            mapper.updateById(t);
            audit.log("EDIT", "cul_case_tag", t.getId(), "调整标签激励 " + old.getTagName());
        }
        return t;
    }

    public void toggle(Long id) {
        CaseTag t = mapper.selectById(id);
        CaseFlow.require(t != null, "01", "标签不存在");
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        t.setStatus("启用".equals(t.getStatus()) ? "停用" : "启用");
        t.setUpdateTime(LocalDateTime.now());
        mapper.updateById(t);
        audit.log("TOGGLE", "cul_case_tag", id, "标签状态切换为" + t.getStatus());
    }

    /** 取案例首个标签对应的启用配置，无则返回全局默认值。 */
    public CaseTag matchByFirstTag(String valueTagsCsv) {
        if (valueTagsCsv == null || valueTagsCsv.isBlank()) return null;
        String first = valueTagsCsv.split(",")[0].trim();
        if (first.isEmpty()) return null;
        return mapper.selectOne(new LambdaQueryWrapper<CaseTag>()
                .eq(CaseTag::getTagName, first).eq(CaseTag::getStatus, "启用").last("limit 1"));
    }
}
