package com.example.culturemgt.survey.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.survey.entity.SurveyRectify;
import com.example.culturemgt.survey.mapper.SurveyRectifyMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 氛围短板整改BPM：
 * 待方案提交 →(责任人提交方案)→ 方案待审核 →(专员通过)→ 整改实施中
 *   →(提交验收材料)→ 待验收 →(通过)→ 验收闭环 / (不通过)→ 验收驳回重整改 →(补材料)→ 待验收
 * 方案审核驳回回 待方案提交。
 */
@Service
public class RectifyService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private SurveyRectifyMapper mapper;
    @Autowired private AuditService audit;

    public List<SurveyRectify> list(String status, String dept, String owner, Boolean overdueOnly) {
        LambdaQueryWrapper<SurveyRectify> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            w.eq(SurveyRectify::getStatus, status);
        }
        if (dept != null && !dept.isBlank()) {
            w.eq(SurveyRectify::getDept, dept);
        }
        if (owner != null && !owner.isBlank()) {
            w.eq(SurveyRectify::getOwner, owner);
        }
        w.orderByDesc(SurveyRectify::getId);
        List<SurveyRectify> rs = mapper.selectList(w);
        if (Boolean.TRUE.equals(overdueOnly)) {
            java.time.LocalDate today = java.time.LocalDate.now();
            rs = rs.stream().filter(r -> !"验收闭环".equals(r.getStatus())
                    && r.getDeadline() != null && r.getDeadline().isBefore(today)).toList();
        }
        return rs;
    }

    public SurveyRectify get(Long id) {
        SurveyRectify r = mapper.selectById(id);
        SurveyFlow.require(r != null, "01", "整改任务不存在");
        return r;
    }

    /** 系统识别短板后创建整改任务（文化岗）。 */
    public SurveyRectify create(SurveyRectify body) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        SurveyFlow.require(SurveyFlow.len(body.getTitle()) >= 2 && SurveyFlow.len(body.getTitle()) <= 100,
                "01", "整改任务名称需2-100字");
        SurveyFlow.require(SurveyFlow.len(body.getDept()) > 0 && SurveyFlow.len(body.getOwner()) > 0,
                "01", "责任部门与责任人必填");
        body.setId(null);
        body.setStatus("待方案提交");
        body.setCreator(CurrentHeader.operator());
        mapper.insert(body);
        audit.log("RECT_NEW", "cul_survey_rectify", body.getId(),
                "创建整改任务：" + body.getTitle() + "（" + body.getDept() + "/" + body.getOwner() + "）短板：" + body.getSourceTag());
        return body;
    }

    /** 责任人提交整改方案（方案被驳回后可再次提交）。 */
    @Transactional
    public SurveyRectify submitPlan(Long id, String plan) {
        SurveyRectify r = get(id);
        String me = CurrentHeader.operator();
        boolean staff = CulRole.hasAny(CulRole.CULTURE_STAFF);
        SurveyFlow.require(staff || me.equals(r.getOwner()), "15", "仅责任人或文化岗可提交整改方案");
        SurveyFlow.require("待方案提交".equals(r.getStatus()), "13", "当前状态不可提交方案（须为待方案提交）");
        SurveyFlow.require(SurveyFlow.len(plan) >= 20, "01", "整改方案不少于20字，须含措施与节点");
        r.setPlan(plan.trim());
        r.setPlanSubmitTime(LocalDateTime.now());
        r.setStatus("方案待审核");
        mapper.updateById(r);
        audit.log("RECT_PLAN", "cul_survey_rectify", id, r.getOwner() + " 提交整改方案");
        return r;
    }

    /** 文化专员审核方案：通过→整改实施中；驳回→待方案提交。 */
    public SurveyRectify reviewPlan(Long id, boolean pass, String opinion) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        SurveyRectify r = get(id);
        SurveyFlow.require("方案待审核".equals(r.getStatus()), "13", "当前状态不可审核方案（须为方案待审核）");
        SurveyFlow.require(SurveyFlow.len(opinion) >= 5, "14", "审核意见不少于5字");
        r.setReviewOpinion(opinion.trim());
        r.setReviewTime(LocalDateTime.now());
        r.setStatus(pass ? "整改实施中" : "待方案提交");
        mapper.updateById(r);
        audit.log(pass ? "RECT_PLAN_OK" : "RECT_PLAN_REJECT", "cul_survey_rectify", id,
                "方案审核" + (pass ? "通过" : "驳回") + "：" + opinion.trim());
        return r;
    }

    /** 责任人提交验收材料（含实施说明）；验收驳回后补材料再提交。 */
    @Transactional
    public SurveyRectify submitEvidence(Long id, String implementNote, String evidenceJson) {
        SurveyRectify r = get(id);
        String me = CurrentHeader.operator();
        boolean staff = CulRole.hasAny(CulRole.CULTURE_STAFF);
        SurveyFlow.require(staff || me.equals(r.getOwner()), "15", "仅责任人或文化岗可提交验收材料");
        SurveyFlow.require("整改实施中".equals(r.getStatus()) || "验收驳回重整改".equals(r.getStatus()),
                "13", "当前状态不可提交验收（须为整改实施中）");
        SurveyFlow.require(SurveyFlow.len(implementNote) >= 10, "01", "实施情况说明不少于10字");
        int fileCount = countFiles(evidenceJson);
        SurveyFlow.require(fileCount >= 1, "19", "验收材料缺失，至少上传1份佐证文件");
        r.setImplementNote(implementNote.trim());
        r.setEvidence(evidenceJson);
        r.setStatus("待验收");
        mapper.updateById(r);
        audit.log("RECT_EVIDENCE", "cul_survey_rectify", id, "提交验收材料" + fileCount + "份");
        return r;
    }

    /** 文化专员验收：通过→验收闭环；不通过→验收驳回重整改。 */
    public SurveyRectify reviewAccept(Long id, boolean pass, String opinion) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        SurveyRectify r = get(id);
        SurveyFlow.require("待验收".equals(r.getStatus()), "13", "当前状态不可验收（须为待验收）");
        SurveyFlow.require(SurveyFlow.len(opinion) >= 5, "14", "验收意见不少于5字");
        LocalDateTime now = LocalDateTime.now();
        r.setReviewOpinion(opinion.trim());
        r.setReviewTime(now);
        if (pass) {
            r.setStatus("验收闭环");
            r.setCloseTime(now);
        } else {
            r.setStatus("验收驳回重整改");
        }
        mapper.updateById(r);
        audit.log(pass ? "RECT_ACCEPT_OK" : "RECT_ACCEPT_REJECT", "cul_survey_rectify", id,
                "验收" + (pass ? "通过闭环" : "驳回重整改") + "：" + opinion.trim());
        return r;
    }

    private int countFiles(String evidenceJson) {
        if (evidenceJson == null || evidenceJson.isBlank()) {
            return 0;
        }
        try {
            return MAPPER.readTree(evidenceJson).size();
        } catch (Exception e) {
            return 0;
        }
    }
}
