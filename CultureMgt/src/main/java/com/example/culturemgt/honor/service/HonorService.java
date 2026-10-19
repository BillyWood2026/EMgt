package com.example.culturemgt.honor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.casehub.entity.CaseItem;
import com.example.culturemgt.casehub.mapper.CaseItemMapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.foundation.service.FoundationFlow;
import com.example.culturemgt.honor.entity.HonorApply;
import com.example.culturemgt.honor.entity.HonorProject;
import com.example.culturemgt.honor.mapper.HonorApplyMapper;
import com.example.culturemgt.honor.mapper.HonorProjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 荣誉与榜样：项目配置 + 申报评审公示表彰全流程。
 * 状态机：草稿→已申报→部门初审→评审中→公示中（可异议）→终审表彰 / 评审驳回。
 * 否决算法：周期内存在重大文化红线违规记录（反面警示案例）自动拦截申报。
 */
@Service
public class HonorService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private HonorProjectMapper projectMapper;
    @Autowired private HonorApplyMapper applyMapper;
    @Autowired private CaseItemMapper caseMapper;
    @Autowired private AuditService audit;

    // ---------- 荣誉项目 ----------

    public List<HonorProject> projects(String status) {
        LambdaQueryWrapper<HonorProject> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(HonorProject::getStatus, status);
        w.orderByAsc(HonorProject::getId);
        return projectMapper.selectList(w);
    }

    public HonorProject saveProject(HonorProject p) {
        CulRole.requireAny(CulRole.C03, CulRole.C07, CulRole.C99);
        HonorFlow.require(HonorFlow.len(p.getProjectName()) >= 2 && HonorFlow.len(p.getProjectName()) <= 100, "01", "项目名称需2-100字");
        HonorFlow.require(p.getPeriodType() != null && List.of("月度", "季度", "半年度", "年度").contains(p.getPeriodType()), "01", "评选周期须为 月度/季度/半年度/年度");
        HonorFlow.require(p.getApplyType() != null && List.of("个人", "团队").contains(p.getApplyType()), "01", "申报类型须为 个人/团队");
        HonorFlow.require(p.getApplyStart() != null && p.getApplyEnd() != null, "01", "申报起止时间必填");
        HonorFlow.require(!p.getApplyEnd().isBefore(p.getApplyStart()), "01", "申报截止不得早于开始时间");
        Long dup = projectMapper.selectCount(new LambdaQueryWrapper<HonorProject>()
                .eq(HonorProject::getProjectName, p.getProjectName())
                .ne(p.getId() != null, HonorProject::getId, p.getId() == null ? -1L : p.getId())
                .eq(HonorProject::getDeleted, 0));
        HonorFlow.require(dup == 0, "13", "荣誉项目名称已存在");
        if (p.getId() == null) {
            p.setStatus("已启用");
            p.setAutoSyncCase(p.getAutoSyncCase() == null ? 0 : p.getAutoSyncCase());
            p.setOperator(CurrentHeader.operator());
            p.setCreateTime(LocalDateTime.now());
            projectMapper.insert(p);
            audit.log("NEW", "cul_honor_project", p.getId(), "新增荣誉项目 " + p.getProjectName());
        } else {
            HonorProject old = projectMapper.selectById(p.getId());
            HonorFlow.require(old != null, "01", "荣誉项目不存在");
            p.setStatus(old.getStatus());
            p.setUpdateTime(LocalDateTime.now());
            projectMapper.updateById(p);
            audit.log("EDIT", "cul_honor_project", p.getId(), "调整荣誉项目 " + p.getProjectName());
        }
        return p;
    }

    public void toggleProject(Long id) {
        CulRole.requireAny(CulRole.C03, CulRole.C07, CulRole.C99);
        HonorProject p = projectMapper.selectById(id);
        HonorFlow.require(p != null, "01", "荣誉项目不存在");
        HonorProject u = new HonorProject();
        u.setId(id);
        u.setStatus("已启用".equals(p.getStatus()) ? "已停用" : "已启用");
        u.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(u);
        audit.log("TOGGLE", "cul_honor_project", id, "荣誉项目" + u.getStatus());
    }

    // ---------- 申报查询 ----------

    /** 榜样广场：终审表彰记录，全员可见。 */
    public List<HonorApply> square(String keyword) {
        LambdaQueryWrapper<HonorApply> w = new LambdaQueryWrapper<>();
        w.eq(HonorApply::getStatus, "终审表彰");
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(HonorApply::getCandidate, keyword).or().like(HonorApply::getProjectName, keyword));
        }
        w.orderByDesc(HonorApply::getAwardTime).orderByDesc(HonorApply::getId);
        return applyMapper.selectList(w);
    }

    /** 公示中列表（全员可查看并可提异议）。 */
    public List<HonorApply> publicity() {
        return applyMapper.selectList(new LambdaQueryWrapper<HonorApply>()
                .eq(HonorApply::getStatus, "公示中").orderByAsc(HonorApply::getPublicityEnd));
    }

    public List<HonorApply> mine() {
        return applyMapper.selectList(new LambdaQueryWrapper<HonorApply>()
                .eq(HonorApply::getContributor, CurrentHeader.operator())
                .orderByDesc(HonorApply::getId));
    }

    public List<HonorApply> list(String status, Long projectId) {
        CulRole.requireAny(CulRole.BACKSTAGE);
        LambdaQueryWrapper<HonorApply> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(HonorApply::getStatus, status);
        if (projectId != null) w.eq(HonorApply::getProjectId, projectId);
        w.orderByDesc(HonorApply::getId);
        return applyMapper.selectList(w);
    }

    public HonorApply get(Long id) {
        HonorApply a = applyMapper.selectById(id);
        HonorFlow.require(a != null, "01", "申报记录不存在");
        return a;
    }

    // ---------- 申报：保存/提交 ----------

    public HonorApply save(HonorApply a) {
        if (a.getId() == null) {
            HonorFlow.require(a.getProjectId() != null, "01", "申报荣誉项目必填");
            HonorProject p = projectMapper.selectById(a.getProjectId());
            HonorFlow.require(p != null && p.getDeleted() == 0, "01", "荣誉项目不存在");
            if ("个人".equals(p.getApplyType())) {
                a.setCandidate(CurrentHeader.operator()); // 个人选择本人
            } else {
                HonorFlow.require(HonorFlow.len(a.getCandidate()) > 0, "01", "团队申报须填写团队名称");
            }
            HonorFlow.require(HonorFlow.len(a.getUserDept()) > 0, "01", "申报部门必填");
            a.setApplyType(p.getApplyType());
            a.setProjectName(p.getProjectName());
            a.setContributor(CurrentHeader.operator());
            a.setStatus("草稿");
            a.setStage(0);
            a.setApprovalLog("[]");
            a.setObjections("[]");
            a.setSyncCase(0);
            a.setCreateTime(LocalDateTime.now());
            validateBody(a, p);
            applyMapper.insert(a);
            audit.log("NEW", "cul_honor_apply", a.getId(), a.getContributor() + " 新建荣誉申报草稿（" + p.getProjectName() + "）");
        } else {
            HonorApply old = applyMapper.selectById(a.getId());
            HonorFlow.require(old != null, "01", "申报记录不存在");
            HonorFlow.require("草稿".equals(old.getStatus()), "14", "仅草稿可编辑");
            boolean owner = CurrentHeader.operator().equals(old.getContributor());
            HonorFlow.require(owner || CulRole.hasAny(CulRole.CULTURE_STAFF), "14", "仅本人或文化管理岗可编辑草稿");
            HonorProject p = projectMapper.selectById(old.getProjectId());
            a.setStatus("草稿");
            a.setUpdateTime(LocalDateTime.now());
            validateBody(a, p);
            applyMapper.updateById(a);
            audit.log("EDIT", "cul_honor_apply", a.getId(), "编辑荣誉申报草稿");
        }
        return a;
    }

    private void validateBody(HonorApply a, HonorProject p) {
        HonorFlow.require(p != null, "01", "关联荣誉项目不存在");
        HonorFlow.require(HonorFlow.len(a.getDeeds()) >= 100, "02", "先进事迹描述须不少于100字");
        HonorFlow.require(lenJson(a.getAttachments()) >= 1, "12", "佐证附件必填，至少1份");
    }

    private int lenJson(String json) {
        if (json == null || json.isBlank()) return 0;
        try {
            List<?> l = MAPPER.readValue(json, List.class);
            return l.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /** 提交申报：项目启用+申报期内+红线否决+重复拦截 → 已申报。 */
    public void submit(Long id) {
        HonorApply a = applyMapper.selectById(id);
        HonorFlow.require(a != null, "01", "申报记录不存在");
        HonorFlow.require("草稿".equals(a.getStatus()), "14", "仅草稿可提交申报");
        boolean owner = CurrentHeader.operator().equals(a.getContributor());
        HonorFlow.require(owner || CulRole.hasAny(CulRole.CULTURE_STAFF), "14", "仅本人或文化管理岗可提交");
        HonorProject p = projectMapper.selectById(a.getProjectId());
        HonorFlow.require(p != null && p.getDeleted() == 0, "01", "荣誉项目不存在");
        HonorFlow.require("已启用".equals(p.getStatus()), "04", "该荣誉项目已停用，不可申报");
        LocalDate today = LocalDate.now();
        HonorFlow.require(!today.isBefore(p.getApplyStart()) && !today.isAfter(p.getApplyEnd()), "05",
                "不在申报期内（" + p.getApplyStart() + " ~ " + p.getApplyEnd() + "）");
        // 否决算法：周期内存在重大文化红线违规记录（反面警示案例），自动拦截
        Long red = caseMapper.selectCount(new LambdaQueryWrapper<CaseItem>()
                .eq(CaseItem::getCaseType, "反面警示")
                .eq(CaseItem::getDeleted, 0)
                .and(q -> q.eq(CaseItem::getContributor, a.getCandidate())
                        .or().eq(CaseItem::getOrgName, a.getCandidate())));
        HonorFlow.require(red == 0, "03", "该对象存在重大文化红线记录，禁止参与本次荣誉评选");
        // 重复申报拦截：同项目同对象存在在途/已表彰记录
        Long dup = applyMapper.selectCount(new LambdaQueryWrapper<HonorApply>()
                .eq(HonorApply::getProjectId, a.getProjectId())
                .eq(HonorApply::getCandidate, a.getCandidate())
                .eq(HonorApply::getDeleted, 0)
                .in(HonorApply::getStatus, "已申报", "部门初审", "评审中", "公示中", "终审表彰"));
        HonorFlow.require(dup == 0, "06", "该对象本期已有在办或已表彰的申报记录，请勿重复申报");
        HonorApply u = new HonorApply();
        u.setId(id);
        u.setStatus("已申报");
        u.setStage(0);
        u.setApprovalLog(append(a.getApprovalLog(), Map.of("stage", 0, "pass", true, "opinion", "提交申报", "by", CurrentHeader.operator(), "roles", roles(), "time", now())));
        u.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(u);
        audit.log("SUBMIT", "cul_honor_apply", id, a.getCandidate() + " 提交" + a.getProjectName() + "申报");
    }

    // ---------- 评审链 ----------

    /** 部门负责人初审：已申报 → 部门初审（pass）/ 评审驳回。 */
    public void firstReview(Long id, boolean pass, String opinion) {
        CulRole.requireAny(CulRole.C04, CulRole.C99);
        HonorApply a = applyMapper.selectById(id);
        HonorFlow.require(a != null, "01", "申报记录不存在");
        HonorFlow.require("已申报".equals(a.getStatus()), "14", "当前状态不可初审");
        HonorFlow.require(HonorFlow.len(opinion) >= 5, "07", "初审意见为必填项，且不少于5字");
        HonorApply u = new HonorApply();
        u.setId(id);
        u.setReviewOpinion(opinion);
        if (pass) {
            u.setStatus("部门初审");
            u.setStage(1);
        } else {
            u.setStatus("评审驳回");
            u.setStage(-1);
        }
        u.setApprovalLog(append(a.getApprovalLog(), Map.of("stage", 1, "pass", pass, "opinion", opinion, "by", CurrentHeader.operator(), "roles", roles(), "time", now())));
        u.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(u);
        audit.log(pass ? "REVIEW" : "REJECT", "cul_honor_apply", id, "部门初审" + (pass ? "通过" : "驳回") + "：" + opinion);
    }

    /** 文化专员复核：部门初审 → 评审中（pass，自动读取文化综合得分）/ 驳回。 */
    public void review(Long id, boolean pass, String opinion) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        HonorApply a = applyMapper.selectById(id);
        HonorFlow.require(a != null, "01", "申报记录不存在");
        HonorFlow.require("部门初审".equals(a.getStatus()), "14", "当前状态不可复核");
        HonorFlow.require(pass || HonorFlow.len(opinion) >= 5, "07", "驳回时复核意见为必填项，且不少于5字");
        HonorApply u = new HonorApply();
        u.setId(id);
        if (pass) {
            u.setCultureScore(cultureScoreOf(a.getCandidate()));
            u.setStatus("评审中");
            u.setStage(2);
        } else {
            u.setStatus("评审驳回");
            u.setStage(-1);
        }
        u.setReviewOpinion(pass ? (opinion == null || opinion.isBlank() ? "复核通过" : opinion) : opinion);
        u.setApprovalLog(append(a.getApprovalLog(), Map.of("stage", 2, "pass", pass, "opinion", u.getReviewOpinion(), "by", CurrentHeader.operator(), "roles", roles(), "time", now())));
        u.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(u);
        audit.log(pass ? "REVIEW" : "REJECT", "cul_honor_apply", id, "文化专员复核" + (pass ? "通过，文化综合得分 " + u.getCultureScore() : "驳回"));
    }

    /** 文化综合得分：候选人已发布案例的积分合计（自动读取作为评审参考）。 */
    private java.math.BigDecimal cultureScoreOf(String candidate) {
        List<CaseItem> cases = caseMapper.selectList(new LambdaQueryWrapper<CaseItem>()
                .eq(CaseItem::getContributor, candidate)
                .eq(CaseItem::getStatus, "已发布")
                .eq(CaseItem::getDeleted, 0));
        int total = cases.stream().mapToInt(c -> c.getPointTotal() == null ? 0 : c.getPointTotal()).sum();
        return java.math.BigDecimal.valueOf(total);
    }

    /** 发起公示：评审中 → 公示中，公示起止必填。 */
    public void publicize(Long id, LocalDate start, LocalDate end) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        HonorApply a = applyMapper.selectById(id);
        HonorFlow.require(a != null, "01", "申报记录不存在");
        HonorFlow.require("评审中".equals(a.getStatus()), "14", "当前状态不可发起公示");
        HonorFlow.require(start != null && end != null, "08", "公示起止时间必填");
        HonorFlow.require(!end.isBefore(start), "08", "公示结束不得早于开始");
        HonorApply u = new HonorApply();
        u.setId(id);
        u.setPublicityStart(start);
        u.setPublicityEnd(end);
        u.setStatus("公示中");
        u.setStage(3);
        u.setApprovalLog(append(a.getApprovalLog(), Map.of("stage", 3, "pass", true, "opinion", "发起公示 " + start + "~" + end, "by", CurrentHeader.operator(), "roles", roles(), "time", now())));
        u.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(u);
        audit.log("PUBLICIZE", "cul_honor_apply", id, "发起公示 " + start + " ~ " + end);
    }

    /** 异议申诉：公示期内全员可提交，理由必填≥5字。 */
    public void objection(Long id, String reason) {
        HonorApply a = applyMapper.selectById(id);
        HonorFlow.require(a != null, "01", "申报记录不存在");
        HonorFlow.require("公示中".equals(a.getStatus()), "09", "仅公示期内的申报可提交异议");
        LocalDate today = LocalDate.now();
        HonorFlow.require(!today.isBefore(a.getPublicityStart()) && !today.isAfter(a.getPublicityEnd()), "09",
                "当前不在公示期（" + a.getPublicityStart() + " ~ " + a.getPublicityEnd() + "）内");
        HonorFlow.require(HonorFlow.len(reason) >= 5, "09", "异议理由为必填项，且不少于5字");
        Map<String, Object> entry = new HashMap<>();
        entry.put("by", CurrentHeader.operator());
        entry.put("reason", reason);
        entry.put("time", now());
        entry.put("handled", false);
        String old = a.getObjections() == null || a.getObjections().isBlank() ? "[]" : a.getObjections();
        HonorApply u = new HonorApply();
        u.setId(id);
        u.setObjections(append(old, entry));
        u.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(u);
        audit.log("OBJECTION", "cul_honor_apply", id, CurrentHeader.operator() + " 提交公示异议");
    }

    /** 高管终审：公示中 → 终审表彰；勾选自动同步且项目配置允许时，事迹自动生成公开案例。 */
    @Transactional
    public void finalize(Long id, LocalDate awardTime, String certFiles, boolean syncCase) {
        CulRole.requireAny(CulRole.EXEC);
        HonorApply a = applyMapper.selectById(id);
        HonorFlow.require(a != null, "01", "申报记录不存在");
        HonorFlow.require("公示中".equals(a.getStatus()), "10", "仅公示中的申报可终审表彰");
        HonorFlow.require(awardTime != null, "01", "表彰时间必填");
        HonorProject p = projectMapper.selectById(a.getProjectId());
        HonorApply u = new HonorApply();
        u.setId(id);
        u.setAwardTime(awardTime);
        u.setStatus("终审表彰");
        u.setStage(5);
        String opinion = "终审通过，予以表彰";
        if (certFiles != null && !certFiles.isBlank()) u.setCertFiles(certFiles);
        if (p != null && p.getAutoSyncCase() != null && p.getAutoSyncCase() == 1 && syncCase) {
            CaseItem c = new CaseItem();
            c.setCaseTitle("【" + a.getProjectName() + "】" + a.getCandidate() + "表彰事迹（荣誉归档同步）");
            c.setValueTags("奋斗奉献");
            c.setCaseType("正向标杆");
            c.setOccurTime(LocalDateTime.now());
            c.setContent(a.getDeeds());
            c.setAttachments(a.getAttachments());
            c.setIsPublic(1);
            c.setContributor(a.getCandidate());
            c.setOrgName(a.getUserDept());
            c.setStatus("已发布");
            c.setStage(3);
            c.setApprovalLog("[]");
            c.setPointBase(0);
            c.setPointNews(0);
            c.setPointTraining(0);
            c.setPointTotal(0);
            c.setPointDetail("荣誉终审同步生成，不参与积分");
            c.setRefNewsCount(0);
            c.setRefTrainingCount(0);
            c.setPublishTime(LocalDateTime.now());
            c.setCreateTime(LocalDateTime.now());
            caseMapper.insert(c);
            u.setSyncCase(1);
            u.setCaseId(c.getId());
            opinion = "终审通过，予以表彰；事迹已同步案例库（案例ID " + c.getId() + "）";
        } else {
            u.setSyncCase(0);
        }
        u.setReviewOpinion(opinion);
        u.setApprovalLog(append(a.getApprovalLog(), Map.of("stage", 5, "pass", true, "opinion", opinion, "by", CurrentHeader.operator(), "roles", roles(), "time", now())));
        u.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(u);
        audit.log("FINALIZE", "cul_honor_apply", id, a.getCandidate() + " 终审表彰" + (u.getSyncCase() == 1 ? "并同步案例库" : ""));
    }

    /** 评审驳回：C04（已申报）/ 文化岗（部门初审、评审中）/ 高管（公示中）。 */
    public void reject(Long id, String opinion) {
        HonorApply a = applyMapper.selectById(id);
        HonorFlow.require(a != null, "01", "申报记录不存在");
        HonorFlow.require(HonorFlow.len(opinion) >= 5, "07", "驳回意见为必填项，且不少于5字");
        String st = a.getStatus();
        HonorFlow.require(List.of("已申报", "部门初审", "评审中", "公示中").contains(st), "14", "当前状态不可驳回");
        if ("已申报".equals(st)) {
            CulRole.requireAny(CulRole.C04, CulRole.C99);
        } else if ("公示中".equals(st)) {
            CulRole.requireAny(CulRole.EXEC);
        } else {
            CulRole.requireAny(CulRole.CULTURE_STAFF);
        }
        HonorApply u = new HonorApply();
        u.setId(id);
        u.setStatus("评审驳回");
        u.setStage(-1);
        u.setReviewOpinion(opinion);
        u.setApprovalLog(append(a.getApprovalLog(), Map.of("stage", st, "pass", false, "opinion", opinion, "by", CurrentHeader.operator(), "roles", roles(), "time", now())));
        u.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(u);
        audit.log("REJECT", "cul_honor_apply", id, st + " 节点驳回：" + opinion);
    }

    // ---------- 工具 ----------

    private String append(String oldJson, Map<String, Object> entry) {
        Map<String, Object> e = new HashMap<>(entry);
        return FoundationFlow.appendLog(oldJson, e);
    }

    private String roles() {
        return CulRole.currentRoles().stream().sorted().collect(java.util.stream.Collectors.joining(","));
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
