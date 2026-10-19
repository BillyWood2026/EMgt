package com.example.partyaffairsmgt.opinion.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.opinion.entity.OpinionAlert;
import com.example.partyaffairsmgt.opinion.entity.OpinionHandle;
import com.example.partyaffairsmgt.opinion.entity.OpinionHandleRecord;
import com.example.partyaffairsmgt.opinion.mapper.OpinionAlertMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionHandleMapper;
import com.example.partyaffairsmgt.opinion.mapper.OpinionHandleRecordMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 舆情处置流程服务（需求 8.4）。
 * 七环节闭环：发现→研判→转办→处置→反馈→复核→归档；重大舆情启动应急预案。
 * 错误码：E-OP-07 升降级无研判记录阻断；E-OP-08 转办4小时无人认领；E-OP-09 处置超时升级；
 * E-OP-10 反馈材料缺失；E-OP-11 复核未通过阻断归档；E-OP-16 转办必填；E-OP-17 研判结论必填。
 */
@Service
public class OpinionHandleService {

    /** 转办工单无人认领升级时限（4小时） */
    private static final int CLAIM_TIMEOUT_HOURS = 4;

    @Autowired
    private OpinionHandleMapper handleMapper;
    @Autowired
    private OpinionHandleRecordMapper recordMapper;
    @Autowired
    private OpinionAlertMapper alertMapper;
    @Autowired
    private AuditService auditService;

    public List<OpinionHandle> list(String level, String flowStatus, String keyword) {
        QueryWrapper<OpinionHandle> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (level != null && !level.isBlank()) {
            qw.eq("alert_level", level);
        }
        if (flowStatus != null && !flowStatus.isBlank()) {
            qw.eq("flow_status", flowStatus);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like("title", keyword);
        }
        qw.orderByDesc("create_time");
        return handleMapper.selectList(qw);
    }

    public OpinionHandle detail(Long id) {
        OpinionHandle h = handleMapper.selectById(id);
        if (h == null || h.getDeleted() == 1) {
            throw PartyBizException.block("E-OP-10", "处置工单不存在");
        }
        return h;
    }

    public List<OpinionHandleRecord> records(Long handleId) {
        return recordMapper.selectList(new QueryWrapper<OpinionHandleRecord>()
                .eq("handle_id", handleId).orderByAsc("record_time"));
    }

    /** 发现：由预警生成处置工单（同预警重复建单合并提醒 E-OP-13） */
    @Transactional
    public OpinionHandle create(Long alertId) {
        OpinionAlert alert = alertMapper.selectById(alertId);
        if (alert == null || alert.getDeleted() == 1) {
            throw PartyBizException.block("E-OP-10", "预警记录不存在");
        }
        Long dup = handleMapper.selectCount(new QueryWrapper<OpinionHandle>()
                .eq("deleted", 0).eq("alert_id", alertId));
        if (dup != null && dup > 0) {
            throw new PartyBizException("E-OP-13", PartyBizException.WARN,
                    "该预警已存在处置工单，已合并提醒，请勿重复建单");
        }
        OpinionHandle h = new OpinionHandle();
        h.setAlertId(alertId);
        h.setCollectionId(alert.getCollectionId());
        h.setTitle(alert.getTitle());
        h.setAlertLevel(alert.getAlertLevel());
        h.setFlowStatus("发现");
        h.setArchiveStatus("未归档");
        h.setEmergency("红色".equals(alert.getAlertLevel()) ? 1 : 0);
        h.setStatus("待处置");
        h.setCreateTime(LocalDateTime.now());
        handleMapper.insert(h);
        insertRecord(h.getId(), "发现", "由" + alert.getAlertLevel() + "预警生成工单",
                "红色".equals(alert.getAlertLevel()) ? "重大舆情，启动应急预案并上报党委" : null);
        auditService.log("舆情处置", "生成工单", h.getId(), h.getTitle());
        return h;
    }

    /** 研判：属实性、影响面、风险等级（E-OP-17 结论必填） */
    @Transactional
    public OpinionHandle research(Long id, String conclusion, String impact, String riskLevel, Integer emergency) {
        if (conclusion == null || conclusion.isBlank()) {
            throw PartyBizException.block("E-OP-17", "研判结论必填（属实性、影响面、风险等级）");
        }
        OpinionHandle h = detail(id);
        h.setResearchConclusion(conclusion);
        h.setImpact(impact);
        h.setRiskLevel(riskLevel);
        if (emergency != null) {
            h.setEmergency(emergency);
        }
        h.setFlowStatus("研判");
        h.setUpdateTime(LocalDateTime.now());
        handleMapper.updateById(h);
        insertRecord(id, "研判", conclusion, emergency != null && emergency == 1 ? "启动应急预案" : null);
        return h;
    }

    /** 转办：指定承办单位/责任人/时限（E-OP-16 必填校验） */
    @Transactional
    public OpinionHandle transfer(Long id, String unit, String owner, LocalDateTime deadline) {
        if (unit == null || unit.isBlank() || owner == null || owner.isBlank()) {
            throw PartyBizException.block("E-OP-16", "承办单位与责任人为转办必填项");
        }
        if (deadline == null) {
            throw PartyBizException.block("E-OP-16", "处置时限为转办必填项（红24/橙48/黄72小时）");
        }
        OpinionHandle h = detail(id);
        h.setUnit(unit);
        h.setOwner(owner);
        h.setDeadline(deadline);
        h.setFlowStatus("转办");
        h.setUpdateTime(LocalDateTime.now());
        handleMapper.updateById(h);
        insertRecord(id, "转办", "转办至" + unit + "/" + owner + "，时限" + deadline.toLocalDate(), null);
        return h;
    }

    /** 认领（4小时无人认领提醒并升级 E-OP-08） */
    @Transactional
    public OpinionHandle claim(Long id) {
        OpinionHandle h = detail(id);
        h.setClaimTime(LocalDateTime.now());
        h.setUpdateTime(LocalDateTime.now());
        handleMapper.updateById(h);
        boolean late = h.getCreateTime() != null
                && h.getCreateTime().plusHours(CLAIM_TIMEOUT_HOURS).isBefore(LocalDateTime.now());
        insertRecord(id, "转办", "责任人认领" + (late ? "（超4小时，已提醒并升级）" : ""),
                late ? "E-OP-08 转办工单超时未认领，已升级通知上级" : null);
        return h;
    }

    /** 处置：正面回应、澄清、整改等 */
    @Transactional
    public OpinionHandle doHandle(Long id, String measure) {
        if (measure == null || measure.isBlank()) {
            throw PartyBizException.block("E-OP-17", "处置措施必填（回应、澄清、整改安排）");
        }
        OpinionHandle h = detail(id);
        h.setMeasure(measure);
        h.setHandler(CurrentHeader.operator());
        h.setHandleTime(LocalDateTime.now());
        h.setFlowStatus("处置");
        h.setStatus("处置中");
        h.setUpdateTime(LocalDateTime.now());
        handleMapper.updateById(h);
        insertRecord(id, "处置", measure, null);
        return h;
    }

    /** 反馈：处置结果与佐证（材料必填） */
    @Transactional
    public OpinionHandle feedback(Long id, String result, String feedbackFile) {
        OpinionHandle h = detail(id);
        if (result == null || result.isBlank()) {
            throw PartyBizException.block("E-OP-10", "处置结果必填");
        }
        if (feedbackFile == null || feedbackFile.isBlank()) {
            throw PartyBizException.block("E-OP-10", "反馈材料（处置结果佐证）必填");
        }
        h.setResult(result);
        h.setFeedbackFile(feedbackFile);
        h.setFlowStatus("反馈");
        h.setUpdateTime(LocalDateTime.now());
        handleMapper.updateById(h);
        insertRecord(id, "反馈", result, "佐证：" + feedbackFile);
        return h;
    }

    /** 复核：通过/退回 */
    @Transactional
    public OpinionHandle review(Long id, boolean passed, String opinion) {
        OpinionHandle h = detail(id);
        h.setReviewer(CurrentHeader.operator());
        h.setReviewTime(LocalDateTime.now());
        h.setReviewResult(passed ? "通过" : "退回");
        h.setFlowStatus(passed ? "复核" : "处置");
        h.setUpdateTime(LocalDateTime.now());
        handleMapper.updateById(h);
        insertRecord(id, "复核", (passed ? "复核通过" : "复核退回") + (opinion == null ? "" : "：" + opinion), null);
        return h;
    }

    /** 归档：复核通过且反馈材料齐全（E-OP-10/E-OP-11） */
    @Transactional
    public OpinionHandle archive(Long id) {
        OpinionHandle h = detail(id);
        if (h.getFeedbackFile() == null || h.getFeedbackFile().isBlank()) {
            throw PartyBizException.block("E-OP-10", "处置反馈材料缺失，阻断归档");
        }
        if (!"通过".equals(h.getReviewResult())) {
            throw PartyBizException.block("E-OP-11", "复核未通过或未复核，阻断归档");
        }
        h.setFlowStatus("归档");
        h.setArchiveStatus("已归档");
        h.setArchiveTime(LocalDateTime.now());
        h.setStatus("已办结");
        h.setUpdateTime(LocalDateTime.now());
        handleMapper.updateById(h);
        if (h.getAlertId() != null) {
            OpinionAlert alert = alertMapper.selectById(h.getAlertId());
            if (alert != null) {
                alert.setStatus("已解除");
                alert.setUpdateTime(LocalDateTime.now());
                alertMapper.updateById(alert);
            }
        }
        insertRecord(id, "归档", "工单归档闭环", null);
        auditService.log("舆情处置", "归档", id, h.getTitle());
        return h;
    }

    /** 舆情升级/降级：须有研判记录（E-OP-07） */
    @Transactional
    public OpinionHandle changeLevel(Long id, String newLevel) {
        OpinionHandle h = detail(id);
        Long researchCount = recordMapper.selectCount(new QueryWrapper<OpinionHandleRecord>()
                .eq("handle_id", id).eq("node", "研判"));
        if (researchCount == null || researchCount == 0) {
            throw PartyBizException.block("E-OP-07", "无研判记录，阻断等级变更（升级/降级须先完成研判）");
        }
        h.setAlertLevel(newLevel);
        h.setUpdateTime(LocalDateTime.now());
        handleMapper.updateById(h);
        if (h.getAlertId() != null) {
            OpinionAlert alert = alertMapper.selectById(h.getAlertId());
            if (alert != null) {
                alert.setAlertLevel(newLevel);
                alert.setUpdateTime(LocalDateTime.now());
                alertMapper.updateById(alert);
            }
        }
        insertRecord(id, "升级", "舆情等级调整为" + newLevel, null);
        return h;
    }

    /** 时限看板：环节分布、超时清单、超时升级（E-OP-09） */
    @Transactional
    public Map<String, Object> dashboard() {
        List<OpinionHandle> all = handleMapper.selectList(
                new QueryWrapper<OpinionHandle>().eq("deleted", 0));
        Map<String, Object> r = new LinkedHashMap<>();
        Map<String, Long> byLevel = new LinkedHashMap<>();
        Map<String, Long> byFlow = new LinkedHashMap<>();
        int overdue = 0;
        int unclaimedLate = 0;
        for (OpinionHandle h : all) {
            byLevel.merge(h.getAlertLevel() == null ? "未知" : h.getAlertLevel(), 1L, Long::sum);
            byFlow.merge(h.getFlowStatus() == null ? "发现" : h.getFlowStatus(), 1L, Long::sum);
            boolean overdueFlag = h.getDeadline() != null
                    && h.getDeadline().isBefore(LocalDateTime.now())
                    && !"已归档".equals(h.getArchiveStatus());
            if (overdueFlag) {
                overdue++;
                // 处置超时自动升级告警并通知上级（每单只记一次）
                Long escalated = recordMapper.selectCount(new QueryWrapper<OpinionHandleRecord>()
                        .eq("handle_id", h.getId()).eq("node", "升级").like("conclusion", "处置超时"));
                if (escalated == null || escalated == 0) {
                    insertRecord(h.getId(), "升级", "处置超时（E-OP-09），自动升级告警并通知上级", null);
                }
            }
            if (h.getClaimTime() == null && h.getCreateTime() != null
                    && h.getCreateTime().plusHours(CLAIM_TIMEOUT_HOURS).isBefore(LocalDateTime.now())
                    && !"已归档".equals(h.getArchiveStatus())) {
                unclaimedLate++;
            }
        }
        long closed = all.stream().filter(h -> "已归档".equals(h.getArchiveStatus())).count();
        r.put("total", all.size());
        r.put("byLevel", byLevel);
        r.put("byFlow", byFlow);
        r.put("overdue", overdue);
        r.put("unclaimedLate", unclaimedLate);
        r.put("closed", closed);
        r.put("closeRate", all.isEmpty() ? 0 : Math.round(closed * 1000.0 / all.size()) / 10.0);
        return r;
    }

    private void insertRecord(Long handleId, String node, String conclusion, String remark) {
        OpinionHandleRecord rec = new OpinionHandleRecord();
        rec.setHandleId(handleId);
        rec.setNode(node);
        rec.setOperator(CurrentHeader.operator());
        rec.setRecordTime(LocalDateTime.now());
        rec.setConclusion(conclusion);
        rec.setRemark(remark);
        rec.setCreateTime(LocalDateTime.now());
        recordMapper.insert(rec);
    }
}
