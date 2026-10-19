package com.example.culturemgt.training.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.training.entity.Cert;
import com.example.culturemgt.training.entity.Exam;
import com.example.culturemgt.training.entity.ExamInstance;
import com.example.culturemgt.training.entity.Question;
import com.example.culturemgt.training.mapper.CertMapper;
import com.example.culturemgt.training.mapper.ExamInstanceMapper;
import com.example.culturemgt.training.mapper.ExamMapper;
import com.example.culturemgt.training.mapper.QuestionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 考试任务与监考：自动组卷（引用题库）、自动阅卷、防切屏、补考次数限制。
 * 强制规则：考试不及格自动冻结评优/晋升/调薪资格并写入人事档案（模拟留痕），补考通过自动解除冻结。
 * 状态机【考试实例】待考试 → 已完成合格 / 已完成不合格待补考 → 补考完成合格 / 补考仍不合格。
 */
@Service
public class ExamService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private ExamMapper examMapper;
    @Autowired private ExamInstanceMapper instanceMapper;
    @Autowired private QuestionMapper questionMapper;
    @Autowired private CertMapper certMapper;
    @Autowired private AuditService audit;

    // ---------- 考试任务配置 ----------

    public List<Exam> list(String status) {
        LambdaQueryWrapper<Exam> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(Exam::getStatus, status);
        w.orderByDesc(Exam::getId);
        return examMapper.selectList(w);
    }

    public Exam save(Exam e) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        TrainingFlow.require(TrainingFlow.len(e.getExamName()) >= 2 && TrainingFlow.len(e.getExamName()) <= 100,
                "01", "考试名称需2-100字");
        List<Long> bank = QuestionService.parseBank(e.getBankIds());
        TrainingFlow.require(bank.size() >= 5, "07", "引用题库题目数须不少于5题（当前" + bank.size() + "题）");
        Set<Long> uniq = new HashSet<>(bank);
        for (Long qid : bank) {
            TrainingFlow.require(questionMapper.selectById(qid) != null, "07", "引用题目不存在（ID " + qid + "）");
        }
        TrainingFlow.require(uniq.size() == bank.size(), "07", "引用题库存在重复题目");
        TrainingFlow.require(e.getDurationMin() != null && e.getDurationMin() >= 5, "01", "考试时长须不少于5分钟");
        TrainingFlow.require(e.getPassLine() != null && e.getPassLine() >= 0 && e.getPassLine() <= 100, "12", "及格分数线须在0-100之间");
        TrainingFlow.require(e.getMaxRetake() != null && e.getMaxRetake() >= 0, "01", "允许补考次数须≥0");
        TrainingFlow.require(e.getStartTime() != null && e.getEndTime() != null, "01", "考试起止时间必填");
        TrainingFlow.require(e.getEndTime().isAfter(e.getStartTime()), "01", "考试结束时间须晚于开始时间");
        if (e.getAntiSwitch() == null) e.setAntiSwitch(1);
        if (e.getId() == null) {
            e.setStatus("已启用");
            e.setOperator(CulRole.currentRoles().stream().sorted().findFirst().orElse("C03"));
            e.setCreateTime(LocalDateTime.now());
            examMapper.insert(e);
            audit.log("NEW", "cul_exam", e.getId(), "新增考试任务 " + e.getExamName() + "（组卷" + bank.size() + "题）");
        } else {
            TrainingFlow.require(examMapper.selectById(e.getId()) != null, "01", "考试任务不存在");
            e.setStatus(e.getStatus() == null ? "已启用" : e.getStatus());
            e.setUpdateTime(LocalDateTime.now());
            examMapper.updateById(e);
            audit.log("EDIT", "cul_exam", e.getId(), "调整考试任务 " + e.getExamName());
        }
        return e;
    }

    public void toggle(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        Exam e = examMapper.selectById(id);
        TrainingFlow.require(e != null, "01", "考试任务不存在");
        Exam u = new Exam();
        u.setId(id);
        u.setStatus("已启用".equals(e.getStatus()) ? "已停用" : "已启用");
        u.setUpdateTime(LocalDateTime.now());
        examMapper.updateById(u);
        audit.log("TOGGLE", "cul_exam", id, "考试任务" + u.getStatus());
    }

    public void delete(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        Exam e = examMapper.selectById(id);
        TrainingFlow.require(e != null, "01", "考试任务不存在");
        Long refs = instanceMapper.selectCount(new LambdaQueryWrapper<ExamInstance>()
                .eq(ExamInstance::getExamId, id).eq(ExamInstance::getDeleted, 0));
        TrainingFlow.require(refs == 0, "01", "该考试已有 " + refs + " 条考试实例，禁止删除；可停用考试");
        examMapper.deleteById(id);
        audit.log("DELETE", "cul_exam", id, "删除考试任务 " + e.getExamName());
    }

    /** 分配考试对象：生成待考试实例（同考试同人跳过）。 */
    @Transactional
    public Map<String, Object> assign(Long id, List<Map<String, String>> users) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        Exam e = examMapper.selectById(id);
        TrainingFlow.require(e != null, "01", "考试任务不存在");
        TrainingFlow.require(users != null && !users.isEmpty(), "01", "分配人员名单不能为空");
        int created = 0, skipped = 0;
        for (Map<String, String> u : users) {
            String name = u.get("userName") == null ? "" : u.get("userName").trim();
            TrainingFlow.require(TrainingFlow.len(name) > 0, "01", "人员账号不能为空");
            Long dup = instanceMapper.selectCount(new LambdaQueryWrapper<ExamInstance>()
                    .eq(ExamInstance::getExamId, id).eq(ExamInstance::getUserName, name)
                    .eq(ExamInstance::getDeleted, 0));
            if (dup > 0) { skipped++; continue; }
            ExamInstance i = new ExamInstance();
            i.setExamId(id);
            i.setExamName(e.getExamName());
            i.setUserName(name);
            i.setUserDept(u.get("userDept") == null ? "" : u.get("userDept").trim());
            i.setStatus("待考试");
            i.setAttempt(0);
            i.setSwitchCount(0);
            i.setSwitchLog("[]");
            i.setFrozen(0);
            i.setCreateTime(LocalDateTime.now());
            instanceMapper.insert(i);
            created++;
        }
        audit.log("ASSIGN", "cul_exam", id, "分配考试 " + e.getExamName() + "：新增" + created + " 人，跳过" + skipped + " 人");
        Map<String, Object> r = new HashMap<>();
        r.put("created", created);
        r.put("skipped", skipped);
        return r;
    }

    /** 监考列表：该考试全部实例（含切屏次数、冻结状态）。 */
    public Map<String, Object> instances(Long id) {
        Exam e = examMapper.selectById(id);
        TrainingFlow.require(e != null, "01", "考试任务不存在");
        List<ExamInstance> list = instanceMapper.selectList(new LambdaQueryWrapper<ExamInstance>()
                .eq(ExamInstance::getExamId, id).orderByAsc(ExamInstance::getId));
        Map<String, Object> m = new HashMap<>();
        m.put("exam", e);
        m.put("instances", list);
        return m;
    }

    // ---------- 应考：开考/切屏上报/交卷 ----------

    /** 开考（首次或补考）：校验时间窗/补考次数，记录本次开始时间并返回卷面（不含答案）。 */
    public Map<String, Object> start(Long instanceId) {
        ExamInstance i = instanceMapper.selectById(instanceId);
        TrainingFlow.require(i != null, "01", "考试实例不存在");
        Exam e = examMapper.selectById(i.getExamId());
        TrainingFlow.require(e != null && "已启用".equals(e.getStatus()), "01", "考试任务不存在或已停用");
        LocalDateTime now = LocalDateTime.now();
        // 仅 已完成不合格待补考 可补考；待考试（含开考未交卷再次进入）按首次作答处理
        boolean retake = "已完成不合格待补考".equals(i.getStatus());
        if (!retake) {
            TrainingFlow.require("待考试".equals(i.getStatus()), "09", "当前状态不可开考");
        }
        TrainingFlow.require(!now.isBefore(e.getStartTime()) && !now.isAfter(e.getEndTime()), "08",
                "不在考试时间窗（" + fmt(e.getStartTime()) + " ~ " + fmt(e.getEndTime()) + "）内");
        int attempt = retake ? (i.getAttempt() == null ? 1 : i.getAttempt()) + 1 : 1;
        TrainingFlow.require(attempt <= 1 + e.getMaxRetake(), "11",
                "补考次数已用尽（共1+" + e.getMaxRetake() + "次机会），资格冻结需联系人力处理");
        // 显式清空上次作答痕迹（DATETIME 秒级截断下 updateById 无法置 null）
        instanceMapper.update(null, new LambdaUpdateWrapper<ExamInstance>()
                .eq(ExamInstance::getId, instanceId)
                .set(ExamInstance::getAttempt, attempt)
                .set(ExamInstance::getStartTime, now)
                .set(ExamInstance::getSubmitTime, null)
                .set(ExamInstance::getScore, null)
                .set(ExamInstance::getSwitchCount, 0)
                .set(ExamInstance::getSwitchLog, "[]")
                .set(ExamInstance::getAnswers, "{}")
                .set(ExamInstance::getUpdateTime, now));
        audit.log("START", "cul_exam_instance", instanceId, i.getUserName() + " 开考《" + e.getExamName() + "》第" + attempt + "次");
        // 卷面
        List<Map<String, Object>> paper = new ArrayList<>();
        List<Question> qs = questionsOf(e.getBankIds());
        for (Question q : qs) {
            Map<String, Object> pq = new LinkedHashMap<>();
            pq.put("id", q.getId());
            pq.put("questionType", q.getQuestionType());
            pq.put("stem", q.getStem());
            pq.put("options", optionsOf(q));
            paper.add(pq);
        }
        Map<String, Object> r = new HashMap<>();
        r.put("instance", instanceMapper.selectById(instanceId));
        r.put("durationMin", e.getDurationMin());
        r.put("antiSwitch", e.getAntiSwitch());
        r.put("passLine", e.getPassLine());
        r.put("attempt", attempt);
        r.put("serverStart", now);
        r.put("paper", paper);
        return r;
    }

    /** 防切屏监考：前端 visibilitychange 上报累计；防切屏开启且切屏≥3次由前端强制交卷。 */
    public ExamInstance reportSwitch(Long instanceId, Integer count) {
        ExamInstance i = instanceMapper.selectById(instanceId);
        TrainingFlow.require(i != null, "01", "考试实例不存在");
        Exam e = examMapper.selectById(i.getExamId());
        TrainingFlow.require(e != null && e.getAntiSwitch() != null && e.getAntiSwitch() == 1, "01", "该考试未开启防切屏");
        TrainingFlow.require(count != null && count >= 1, "01", "切屏次数须≥1");
        int total = (i.getSwitchCount() == null ? 0 : i.getSwitchCount()) + count;
        String log = appendJson(i.getSwitchLog(), Map.of("time", fmt(LocalDateTime.now()), "count", count));
        ExamInstance u = new ExamInstance();
        u.setId(instanceId);
        u.setSwitchCount(total);
        u.setSwitchLog(log);
        u.setUpdateTime(LocalDateTime.now());
        instanceMapper.updateById(u);
        audit.log("SWITCH", "cul_exam_instance", instanceId, i.getUserName() + " 考试切屏累计 " + total + " 次");
        return instanceMapper.selectById(instanceId);
    }

    /** 交卷自动阅卷：判分→状态机推进→不及格自动冻结资格→合格自动解冻+发认证证书。 */
    @Transactional
    public Map<String, Object> submit(Long instanceId, Map<String, String> answers, Boolean autoSubmit) {
        ExamInstance i = instanceMapper.selectById(instanceId);
        TrainingFlow.require(i != null, "01", "考试实例不存在");
        Exam e = examMapper.selectById(i.getExamId());
        TrainingFlow.require(e != null, "01", "考试任务不存在");
        TrainingFlow.require(i.getStartTime() != null, "08", "尚未开考，请先开考获取试卷");
        // 已交卷判定：本次作答（start_time）之后已有提交记录；补考重开考后 start_time 刷新即允许再次提交
        boolean submitted = i.getSubmitTime() != null && !i.getSubmitTime().isBefore(i.getStartTime());
        TrainingFlow.require(!submitted, "09", "本次考试已交卷" +
                ("已完成不合格待补考".equals(i.getStatus()) || "补考仍不合格".equals(i.getStatus()) ? "，可申请补考" : ""));
        LocalDateTime now = LocalDateTime.now();
        // 超时校验（含2分钟提交宽限），超时作废本次作答回到待考状态
        LocalDateTime deadline = i.getStartTime().plusMinutes(e.getDurationMin()).plusMinutes(2);
        if (now.isAfter(deadline)) {
            instanceMapper.update(null, new LambdaUpdateWrapper<ExamInstance>()
                    .eq(ExamInstance::getId, instanceId)
                    .set(ExamInstance::getStartTime, null)
                    .set(ExamInstance::getUpdateTime, now));
            TrainingFlow.require(false, "10", "考试已超时（时长" + e.getDurationMin() + "分钟），本次作答作废，请重新开考");
        }
        // 阅卷
        List<Question> qs = questionsOf(e.getBankIds());
        double per = qs.isEmpty() ? 0 : 100.0 / qs.size();
        double got = 0;
        Map<String, String> ans = answers == null ? Map.of() : answers;
        StringBuilder detail = new StringBuilder();
        for (Question q : qs) {
            String given = ans.getOrDefault(String.valueOf(q.getId()), "");
            if (correct(q, given)) {
                got += per;
                detail.append(q.getId()).append(":对 ");
            } else {
                detail.append(q.getId()).append(":错 ");
            }
        }
        int score = (int) Math.round(got);
        boolean pass = score >= e.getPassLine();
        String status;
        if (pass) {
            status = (i.getAttempt() != null && i.getAttempt() > 1) ? "补考完成合格" : "已完成合格";
        } else {
            status = (i.getAttempt() != null && i.getAttempt() > 1) ? "补考仍不合格" : "已完成不合格待补考";
        }
        ExamInstance u = new ExamInstance();
        u.setId(instanceId);
        u.setScore(score);
        u.setStatus(status);
        u.setSubmitTime(now);
        try {
            u.setAnswers(MAPPER.writeValueAsString(ans));
        } catch (Exception ex) {
            u.setAnswers("{}");
        }
        u.setUpdateTime(now);
        instanceMapper.updateById(u);
        String autoNote = Boolean.TRUE.equals(autoSubmit) ? "（防切屏强制交卷）" : "";
        if (pass) {
            // 补考通过自动解除冻结：该人员全部在冻实例解冻
            List<ExamInstance> frozenOnes = instanceMapper.selectList(new LambdaQueryWrapper<ExamInstance>()
                    .eq(ExamInstance::getUserName, i.getUserName()).eq(ExamInstance::getFrozen, 1)
                    .eq(ExamInstance::getDeleted, 0));
            for (ExamInstance f : frozenOnes) {
                ExamInstance fu = new ExamInstance();
                fu.setId(f.getId());
                fu.setFrozen(0);
                fu.setUnfrozenTime(now);
                fu.setUpdateTime(now);
                instanceMapper.updateById(fu);
                audit.log("UNFREEZE", "cul_exam_instance", f.getId(),
                        i.getUserName() + " 文化考试合格，评优/晋升/调薪资格冻结自动解除");
            }
            issueCertIfNeeded(i, e, now);
            audit.log("PASS", "cul_exam_instance", instanceId,
                    i.getUserName() + " 《" + e.getExamName() + "》合格：" + score + " 分" + autoNote);
        } else {
            u.setFrozen(1);
            u.setFrozenTime(now);
            instanceMapper.updateById(u);
            audit.log("FREEZE", "cul_exam_instance", instanceId,
                    i.getUserName() + " 文化考试不合格（" + score + "分），冻结评优/晋升/调薪资格并写入人事档案（模拟）" + autoNote);
        }
        Map<String, Object> r = new HashMap<>();
        r.put("score", score);
        r.put("pass", pass);
        r.put("status", status);
        r.put("detail", detail.toString().trim());
        return r;
    }

    /** 首次合格自动颁发"年度文化合格认证"证书（同一考试同人仅一张）。 */
    private void issueCertIfNeeded(ExamInstance i, Exam e, LocalDateTime now) {
        Long dup = certMapper.selectCount(new LambdaQueryWrapper<Cert>()
                .eq(Cert::getUserName, i.getUserName()).eq(Cert::getSourceExamId, e.getId())
                .eq(Cert::getDeleted, 0));
        if (dup > 0) return;
        Cert c = new Cert();
        int year = Year.now().getValue();
        Long seq = certMapper.selectCount(new LambdaQueryWrapper<Cert>()
                .likeRight(Cert::getCertNo, "CT" + year).eq(Cert::getDeleted, 0));
        c.setCertNo("CT" + year + String.format("%04d", seq + 1));
        c.setUserName(i.getUserName());
        c.setUserDept(i.getUserDept());
        c.setCertType("年度文化合格认证");
        c.setSourceExamId(e.getId());
        c.setReason("《" + e.getExamName() + "》考试合格自动颁发");
        c.setIssueDate(now.toLocalDate());
        c.setIssuer("system");
        c.setCreateTime(now);
        certMapper.insert(c);
        audit.log("CERT", "cul_cert", c.getId(), i.getUserName() + " 获颁年度文化合格认证 " + c.getCertNo());
    }

    // ---------- 查询与统计 ----------

    public List<ExamInstance> myInstances() {
        return instanceMapper.selectList(new LambdaQueryWrapper<ExamInstance>()
                .eq(ExamInstance::getUserName, CurrentHeader.operator()).orderByDesc(ExamInstance::getId));
    }

    /** 算法51 培训合格率 = 考试合格人数 ÷ 参考总人数 ×100%，按部门统计输出看板。 */
    public List<Map<String, Object>> stats(Long examId) {
        LambdaQueryWrapper<ExamInstance> w = new LambdaQueryWrapper<>();
        if (examId != null) w.eq(ExamInstance::getExamId, examId);
        w.eq(ExamInstance::getDeleted, 0);
        List<ExamInstance> all = instanceMapper.selectList(w);
        Map<String, List<ExamInstance>> byDept = new LinkedHashMap<>();
        for (ExamInstance i : all) {
            byDept.computeIfAbsent(i.getUserDept() == null || i.getUserDept().isBlank() ? "未分配" : i.getUserDept(),
                    k -> new ArrayList<>()).add(i);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, List<ExamInstance>> en : byDept.entrySet()) {
            int total = en.getValue().size();
            long pass = en.getValue().stream()
                    .filter(i -> i.getStatus() != null && i.getStatus().contains("合格") && !i.getStatus().contains("不合格"))
                    .count();
            Map<String, Object> m = new HashMap<>();
            m.put("dept", en.getKey());
            m.put("total", total);
            m.put("pass", pass);
            m.put("rate", total == 0 ? 0 : Math.round(pass * 10000.0 / total) / 100.0);
            out.add(m);
        }
        return out;
    }

    // ---------- 工具 ----------

    private List<Question> questionsOf(String bankIds) {
        List<Question> qs = new ArrayList<>();
        for (Long id : QuestionService.parseBank(bankIds)) {
            Question q = questionMapper.selectById(id);
            if (q != null && q.getDeleted() == 0) qs.add(q);
        }
        return qs;
    }

    private List<String> optionsOf(Question q) {
        if (q.getOptionItems() == null || q.getOptionItems().isBlank()) return List.of();
        try {
            List<String> r = new ArrayList<>();
            for (Object o : MAPPER.readValue(q.getOptionItems(), List.class)) r.add(String.valueOf(o));
            return r;
        } catch (Exception e) {
            return List.of();
        }
    }

    /** 判分：单选/判断精确匹配；多选按集合相等（答案逗号分隔规范化）。 */
    private boolean correct(Question q, String given) {
        if (given == null || given.isBlank()) return false;
        Set<String> g = new HashSet<>(Arrays.asList(given.trim().split("[,，]")));
        Set<String> a = new HashSet<>(Arrays.asList(q.getAnswer().split("[,，]")));
        return g.equals(a);
    }

    private String appendJson(String oldJson, Map<String, Object> entry) {
        try {
            List<Object> list = new ArrayList<>();
            if (oldJson != null && !oldJson.isBlank()) {
                list.addAll(MAPPER.readValue(oldJson, List.class));
            }
            list.add(entry);
            return MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            return "[" + entry.size() + "]";
        }
    }

    private String fmt(LocalDateTime t) {
        return t == null ? "" : t.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
