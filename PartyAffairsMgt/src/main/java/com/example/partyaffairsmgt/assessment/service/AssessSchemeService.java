package com.example.partyaffairsmgt.assessment.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.assessment.entity.AssessIndicator;
import com.example.partyaffairsmgt.assessment.entity.AssessScheme;
import com.example.partyaffairsmgt.assessment.entity.AssessTask;
import com.example.partyaffairsmgt.assessment.mapper.AssessIndicatorMapper;
import com.example.partyaffairsmgt.assessment.mapper.AssessSchemeMapper;
import com.example.partyaffairsmgt.assessment.mapper.AssessTaskMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 考核方案服务（需求 10.2）。
 * 发布校验：末级指标权重合计=100%（E-AS-02）；考核对象范围非空（E-AS-03）；
 * 与已发布且未结束方案时间重叠时预警（E-AS-04，可强制发布）；
 * 发布后按考核对象范围自动生成考核任务。
 */
@Service
public class AssessSchemeService {

    @Autowired
    private AssessSchemeMapper schemeMapper;
    @Autowired
    private AssessIndicatorMapper indicatorMapper;
    @Autowired
    private AssessTaskMapper taskMapper;
    @Autowired
    private AuditService auditService;

    public List<AssessScheme> list(String status, Integer year, String keyword) {
        QueryWrapper<AssessScheme> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        if (year != null) qw.eq("assess_year", year);
        if (keyword != null && !keyword.isBlank()) qw.like("scheme_name", keyword);
        qw.orderByDesc("id");
        return schemeMapper.selectList(qw);
    }

    @Transactional
    public AssessScheme save(AssessScheme s) {
        if (s.getSchemeName() == null || s.getSchemeName().isBlank()) {
            throw PartyBizException.block("E-AS-03", "方案名称为必填项");
        }
        if (s.getStatus() == null) s.setStatus("草稿");
        if (s.getPassLine() == null) s.setPassLine(BigDecimal.valueOf(60));
        if (s.getGoodLine() == null) s.setGoodLine(BigDecimal.valueOf(80));
        if (s.getExcellentLine() == null) s.setExcellentLine(BigDecimal.valueOf(90));
        if (s.getExcellentRatio() == null) s.setExcellentRatio(BigDecimal.valueOf(30));
        if (s.getAdjustCap() == null) s.setAdjustCap(BigDecimal.valueOf(5));
        if (s.getAppealDays() == null) s.setAppealDays(5);
        s.setUpdateTime(LocalDateTime.now());
        if (s.getId() == null) {
            s.setCreateTime(LocalDateTime.now());
            schemeMapper.insert(s);
        } else {
            AssessScheme old = schemeMapper.selectById(s.getId());
            if (old != null && "已发布".equals(old.getStatus()) && !"已发布".equals(s.getStatus())) {
                // 允许编辑草稿字段，但已发布方案不允许回退状态
                s.setStatus(old.getStatus());
            }
            schemeMapper.updateById(s);
        }
        auditService.log("党建考核", "保存方案", s.getId(), s.getSchemeName());
        return s;
    }

    /** 权重合计校验（预览用，不阻断保存）。 */
    public Map<String, Object> weightCheck(List<Long> indicatorIds) {
        BigDecimal sum = BigDecimal.ZERO;
        List<Map<String, Object>> items = new ArrayList<>();
        for (Long id : indicatorIds) {
            AssessIndicator i = indicatorMapper.selectById(id);
            if (i == null || i.getDeleted() == 1) continue;
            if (i.getLevel() != 2 && i.getLevel() != 3) continue; // 仅末级参与权重合计
            BigDecimal w = i.getWeight() == null ? BigDecimal.ZERO : i.getWeight();
            sum = sum.add(w);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", i.getId());
            m.put("code", i.getIndicatorCode());
            m.put("name", i.getIndicatorName());
            m.put("weight", w);
            m.put("scoreMode", i.getScoreMode());
            m.put("vetoFlag", i.getVetoFlag());
            items.add(m);
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("totalWeight", sum);
        r.put("valid", sum.compareTo(BigDecimal.valueOf(100)) == 0);
        r.put("items", items);
        return r;
    }

    /**
     * 发布方案：E-AS-02 权重≠100阻断；E-AS-03 对象范围空阻断；
     * E-AS-04 与已发布方案周期重叠预警（force=true 放行）；
     * 通过后生成权重快照与考核任务。
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public AssessScheme publish(Long id, List<Long> indicatorIds, boolean force) {
        AssessScheme s = schemeMapper.selectById(id);
        if (s == null) throw PartyBizException.block("E-AS-03", "方案不存在");
        if ("已发布".equals(s.getStatus())) return s;

        if (s.getTargetOrgIds() == null || s.getTargetOrgIds().isBlank()) {
            throw PartyBizException.block("E-AS-03", "考核对象范围为空，不能发布");
        }
        Map<String, Object> wc = weightCheck(indicatorIds);
        BigDecimal total = (BigDecimal) wc.get("totalWeight");
        if (total.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw PartyBizException.block("E-AS-02",
                    "末级指标权重合计为" + total + "%，不等于100%，阻断发布");
        }
        // 时间重叠：同年度+同对象范围 已有已发布未结束方案
        QueryWrapper<AssessScheme> qw = new QueryWrapper<AssessScheme>()
                .eq("deleted", 0).eq("status", "已发布")
                .eq("target_scope", s.getTargetScope())
                .eq(s.getAssessYear() != null, "assess_year", s.getAssessYear())
                .ne("id", s.getId());
        Long overlap = schemeMapper.selectCount(qw);
        if (overlap > 0 && !force) {
            throw new PartyBizException("E-AS-04", PartyBizException.WARN,
                    "同年度同考核对象范围已存在" + overlap + "个已发布方案，时间重叠（确认继续请勾选强制）");
        }
        // 权重快照
        StringBuilder snap = new StringBuilder("[");
        List<Map<String, Object>> items = (List<Map<String, Object>>) wc.get("items");
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> m = items.get(i);
            if (i > 0) snap.append(',');
            snap.append("{\"indicatorId\":").append(m.get("id"))
                    .append(",\"code\":\"").append(m.get("code")).append("\"")
                    .append(",\"name\":\"").append(m.get("name")).append("\"")
                    .append(",\"weight\":").append(m.get("weight"))
                    .append(",\"scoreMode\":\"").append(m.get("scoreMode")).append("\"")
                    .append(",\"vetoFlag\":").append(m.get("vetoFlag")).append("}");
        }
        snap.append("]");
        s.setWeightSnapshot(snap.toString());
        s.setStatus("已发布");
        s.setPublishTime(LocalDateTime.now());
        s.setUpdateTime(LocalDateTime.now());
        schemeMapper.updateById(s);

        // 生成考核任务（幂等：已有任务的组织跳过）
        String[] orgIds = s.getTargetOrgIds().split(",");
        String[] orgNames = s.getTargetOrgNames() == null ? new String[0] : s.getTargetOrgNames().split(",");
        for (int i = 0; i < orgIds.length; i++) {
            Long orgId = Long.valueOf(orgIds[i].trim());
            Long exist = taskMapper.selectCount(new QueryWrapper<AssessTask>()
                    .eq("deleted", 0).eq("scheme_id", s.getId()).eq("org_id", orgId));
            if (exist > 0) continue;
            AssessTask t = new AssessTask();
            t.setSchemeId(s.getId());
            t.setOrgId(orgId);
            t.setOrgName(i < orgNames.length ? orgNames[i].trim() : ("组织" + orgId));
            t.setStatus("待自评");
            t.setAdjustScore(BigDecimal.ZERO);
            t.setVetoTriggered(0);
            t.setCreateTime(LocalDateTime.now());
            taskMapper.insert(t);
        }
        auditService.log("党建考核", "发布方案", s.getId(), s.getSchemeName() + "，生成" + orgIds.length + "个考核任务");
        return s;
    }

    @Transactional
    public AssessScheme finish(Long id) {
        AssessScheme s = schemeMapper.selectById(id);
        if (s == null) throw PartyBizException.block("E-AS-03", "方案不存在");
        s.setStatus("已结束");
        s.setUpdateTime(LocalDateTime.now());
        schemeMapper.updateById(s);
        auditService.log("党建考核", "结束方案", id, s.getSchemeName());
        return s;
    }
}
