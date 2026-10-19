package com.example.partyaffairsmgt.member.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.FloatCert;
import com.example.partyaffairsmgt.member.entity.FloatContact;
import com.example.partyaffairsmgt.member.entity.FloatingMember;
import com.example.partyaffairsmgt.member.mapper.FloatCertMapper;
import com.example.partyaffairsmgt.member.mapper.FloatContactMapper;
import com.example.partyaffairsmgt.member.mapper.FloatingMemberMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流动党员服务（需求 6.4）。
 * 流出/流入登记、双向联系、活动证管理、失联预警。
 */
@Service
public class FloatingMemberService {

    @Autowired
    private FloatingMemberMapper floatingMapper;
    @Autowired
    private FloatContactMapper contactMapper;
    @Autowired
    private FloatCertMapper certMapper;
    @Autowired
    private AuditService auditService;

    /** 流动党员台账 */
    public List<FloatingMember> list(Long orgId, String flowType, String status, String keyword) {
        QueryWrapper<FloatingMember> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) qw.eq("org_id", orgId);
        if (flowType != null && !flowType.isBlank()) qw.eq("flow_type", flowType);
        if (status != null && !status.isBlank()) qw.eq("flow_status", status);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(q -> q.like("member_name", keyword).or().like("destination", keyword).or().like("cert_no", keyword));
        }
        return floatingMapper.selectList(qw);
    }

    /** 流出登记 */
    @Transactional
    public FloatingMember registerOut(FloatingMember floating) {
        floating.setFlowType("流出");
        floating.setFlowStatus("流出中");
        floating.setFloatAlertStatus("正常");
        floating.setStatus("有效");
        floating.setCreateTime(LocalDateTime.now());
        floatingMapper.insert(floating);
        auditService.log("流动党员", "流出登记", floating.getId(), "党员:" + floating.getMemberName());
        return floating;
    }

    /** 流入登记 */
    @Transactional
    public FloatingMember registerIn(FloatingMember floating) {
        floating.setFlowType("流入");
        floating.setFlowStatus("流出中");
        floating.setFloatAlertStatus("正常");
        floating.setStatus("有效");
        floating.setCreateTime(LocalDateTime.now());
        floatingMapper.insert(floating);
        auditService.log("流动党员", "流入登记", floating.getId(), "党员:" + floating.getMemberName());
        return floating;
    }

    /** 返回销号 */
    @Transactional
    public void returnBack(Long floatingId) {
        FloatingMember fm = floatingMapper.selectById(floatingId);
        if (fm == null || fm.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-09", "流动党员记录不存在");
        }
        fm.setFlowStatus("已返回");
        fm.setStatus("已注销");
        fm.setUpdateTime(LocalDateTime.now());
        floatingMapper.updateById(fm);
        // 注销活动证
        FloatCert cert = certMapper.selectOne(
                new QueryWrapper<FloatCert>().eq("floating_id", floatingId).eq("deleted", 0));
        if (cert != null) {
            cert.setStatus("已注销");
            cert.setUpdateTime(LocalDateTime.now());
            certMapper.updateById(cert);
        }
        auditService.log("流动党员", "返回销号", floatingId, "党员:" + fm.getMemberName());
    }

    /** 双向联系记录 */
    public List<FloatContact> contacts(Long floatingId) {
        return contactMapper.selectList(
                new QueryWrapper<FloatContact>()
                        .eq("floating_id", floatingId)
                        .orderByDesc("contact_date"));
    }

    /** 添加联系记录 */
    @Transactional
    public FloatContact addContact(FloatContact contact) {
        contact.setRecorder(CurrentHeader.operator());
        contact.setCreateTime(LocalDateTime.now());
        contactMapper.insert(contact);
        // 更新最后联系日期
        FloatingMember fm = floatingMapper.selectById(contact.getFloatingId());
        if (fm != null) {
            fm.setLastContactDate(contact.getContactDate());
            fm.setFloatAlertStatus("正常");
            fm.setUpdateTime(LocalDateTime.now());
            floatingMapper.updateById(fm);
        }
        auditService.log("流动党员", "联系记录", contact.getId(), "党员ID:" + contact.getMemberId());
        return contact;
    }

    /** 活动证列表 */
    public List<FloatCert> certs(String status) {
        QueryWrapper<FloatCert> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        return certMapper.selectList(qw);
    }

    /** 发放活动证 */
    @Transactional
    public FloatCert issueCert(Long floatingId, String certNo) {
        FloatingMember fm = floatingMapper.selectById(floatingId);
        if (fm == null || fm.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-09", "流动党员记录不存在");
        }
        FloatCert cert = new FloatCert();
        cert.setFloatingId(floatingId);
        cert.setMemberId(fm.getMemberId());
        cert.setCertNo(certNo);
        cert.setIssueDate(LocalDate.now());
        cert.setIssuer(CurrentHeader.operator());
        cert.setStatus("有效");
        cert.setCreateTime(LocalDateTime.now());
        certMapper.insert(cert);
        // 更新流动记录
        fm.setCertNo(certNo);
        fm.setUpdateTime(LocalDateTime.now());
        floatingMapper.updateById(fm);
        auditService.log("流动党员", "发放活动证", cert.getId(), "证号:" + certNo);
        return cert;
    }

    /** 注销活动证 */
    @Transactional
    public void revokeCert(Long certId) {
        FloatCert cert = certMapper.selectById(certId);
        if (cert == null || cert.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-09", "活动证不存在");
        }
        cert.setStatus("已注销");
        cert.setUpdateTime(LocalDateTime.now());
        certMapper.updateById(cert);
        auditService.log("流动党员", "注销活动证", certId, "证号:" + cert.getCertNo());
    }

    /** 失联预警 */
    public List<Map<String, Object>> alerts() {
        LocalDate today = LocalDate.now();
        List<FloatingMember> list = floatingMapper.selectList(
                new QueryWrapper<FloatingMember>()
                        .eq("flow_status", "流出中")
                        .eq("deleted", 0));
        return list.stream().map(fm -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", fm.getId());
            map.put("memberId", fm.getMemberId());
            map.put("memberName", fm.getMemberName());
            map.put("orgName", fm.getOrgName());
            map.put("destination", fm.getDestination());
            map.put("contactPhone", fm.getContactPhone());
            map.put("lastContactDate", fm.getLastContactDate());
            map.put("contactCycle", fm.getContactCycle());
            // 计算失联时长
            if (fm.getLastContactDate() != null) {
                long months = java.time.temporal.ChronoUnit.MONTHS.between(fm.getLastContactDate(), today);
                map.put("noContactMonths", months);
                if (months >= 6) {
                    map.put("alertLevel", "告警");
                    map.put("alertMessage", "连续6个月未取得联系，建议按自行脱党处理");
                } else if (months >= fm.getContactCycle()) {
                    map.put("alertLevel", "预警");
                    map.put("alertMessage", "超过约定联系周期(" + fm.getContactCycle() + "个月)未联系");
                } else {
                    map.put("alertLevel", "正常");
                    map.put("alertMessage", "");
                }
            } else {
                // 从未联系
                long months = java.time.temporal.ChronoUnit.MONTHS.between(fm.getStartDate(), today);
                map.put("noContactMonths", months);
                if (months >= 6) {
                    map.put("alertLevel", "告警");
                    map.put("alertMessage", "连续6个月未取得联系，建议按自行脱党处理");
                } else if (months >= fm.getContactCycle()) {
                    map.put("alertLevel", "预警");
                    map.put("alertMessage", "超过约定联系周期(" + fm.getContactCycle() + "个月)未联系");
                } else {
                    map.put("alertLevel", "正常");
                    map.put("alertMessage", "");
                }
            }
            return map;
        }).filter(m -> !"正常".equals(m.get("alertLevel"))).collect(Collectors.toList());
    }
}
