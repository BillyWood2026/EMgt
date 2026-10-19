package com.example.partyaffairsmgt.membership.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.PartyMember;
import com.example.partyaffairsmgt.member.mapper.PartyMemberMapper;
import com.example.partyaffairsmgt.membership.entity.DuesPublication;
import com.example.partyaffairsmgt.membership.mapper.DuesPublicationMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 党费收缴情况年度公示（需求 5.3 第9条 / E-DU-10）。 */
@Service
public class DuesPublicationService {

    @Autowired private DuesPublicationMapper publicationMapper;
    @Autowired private DuesExpenseService expenseService;
    @Autowired private PartyMemberMapper memberMapper;
    @Autowired private AuditService auditService;

    public List<DuesPublication> list(Integer year, Long orgId) {
        QueryWrapper<DuesPublication> qw = new QueryWrapper<>();
        if (year != null) qw.eq("fee_year", year);
        if (orgId != null) qw.eq("org_id", orgId);
        qw.orderByDesc("fee_year", "org_id");
        return publicationMapper.selectList(qw);
    }

    /** 新建/编辑年度公示单（金额按年度收支汇总自动带出）。 */
    @Transactional
    public DuesPublication save(DuesPublication body) {
        if (body.getOrgId() == null || body.getFeeYear() == null) {
            throw PartyBizException.block("E-DU-10", "党组织与公示年度必填");
        }
        if (body.getPeriodStart() == null || body.getPeriodEnd() == null) {
            throw PartyBizException.block("E-DU-10", "公示起止时间必填");
        }
        if (body.getPeriodEnd().isBefore(body.getPeriodStart())) {
            throw PartyBizException.block("E-DU-10", "公示截止日不得早于起始日");
        }
        Map<String, Object> s = expenseService.yearSummary(body.getOrgId(), body.getFeeYear());
        body.setTotalCollected(new BigDecimal(s.get("collected").toString()));
        body.setTotalTurnedOver(new BigDecimal(s.get("remitted").toString()));
        body.setTotalUsed(new BigDecimal(s.get("used").toString()));
        body.setBalance(new BigDecimal(s.get("availableBalance").toString()));
        body.setPeriod(body.getFeeYear() + "年度");
        if (body.getPublicScope() == null) body.setPublicScope("全体党员");
        if (body.getId() == null) {
            body.setStatus("草稿");
            body.setCreateTime(LocalDateTime.now());
            publicationMapper.insert(body);
        } else {
            body.setUpdateTime(LocalDateTime.now());
            publicationMapper.updateById(body);
        }
        auditService.log("党费公示单编辑", "DuesPublication", body.getId(),
                body.getOrgName() + " " + body.getFeeYear() + "年度");
        return body;
    }

    @Transactional
    public DuesPublication publish(Long id) {
        DuesPublication p = publicationMapper.selectById(id);
        if (p == null) throw PartyBizException.block("E-DU-10", "公示单不存在");
        if ("已发布".equals(p.getStatus())) {
            throw PartyBizException.block("E-DU-10", "该公示单已发布");
        }
        Long dup = publicationMapper.selectCount(new QueryWrapper<DuesPublication>()
                .eq("org_id", p.getOrgId()).eq("fee_year", p.getFeeYear())
                .eq("status", "已发布").ne(id != null, "id", id));
        if (dup != null && dup > 0) {
            throw PartyBizException.block("E-DU-10", "同一党支部同一年度仅允许发布一次党费收缴情况公示");
        }
        p.setStatus("已发布");
        p.setPublisher(CurrentHeader.operator());
        p.setPublishDate(LocalDate.now());
        p.setUpdateTime(LocalDateTime.now());
        publicationMapper.updateById(p);
        auditService.log("党费公示发布", "DuesPublication", id,
                p.getOrgName() + " " + p.getFeeYear() + "年度党费收缴情况已向党员公布");
        return p;
    }

    /** 年度未公示支部待办（E-DU-10：年底自动生成公示待办）。 */
    public List<Map<String, Object>> todo(Integer year) {
        int y = year == null ? LocalDate.now().getYear() : year;
        List<PartyMember> members = memberMapper.selectList(new QueryWrapper<PartyMember>()
                .in("member_status", "正式党员", "预备党员"));
        Map<Long, String> orgNames = new LinkedHashMap<>();
        for (PartyMember m : members) {
            if (m.getOrgId() != null) orgNames.putIfAbsent(m.getOrgId(), m.getOrgName());
        }
        List<DuesPublication> published = publicationMapper.selectList(new QueryWrapper<DuesPublication>()
                .eq("fee_year", y).eq("status", "已发布"));
        java.util.Set<Long> done = new java.util.HashSet<>();
        published.forEach(p -> done.add(p.getOrgId()));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<Long, String> e : orgNames.entrySet()) {
            if (!done.contains(e.getKey())) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("orgId", e.getKey());
                row.put("orgName", e.getValue());
                row.put("feeYear", y);
                row.put("alert", "该党支部" + y + "年度党费收缴情况尚未向党员公示（E-DU-10）");
                rows.add(row);
            }
        }
        return rows;
    }
}
