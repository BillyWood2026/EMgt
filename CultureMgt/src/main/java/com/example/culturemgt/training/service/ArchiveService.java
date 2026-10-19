package com.example.culturemgt.training.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.training.entity.Cert;
import com.example.culturemgt.training.entity.ExamInstance;
import com.example.culturemgt.training.entity.StudyRecord;
import com.example.culturemgt.training.mapper.CertMapper;
import com.example.culturemgt.training.mapper.ExamInstanceMapper;
import com.example.culturemgt.training.mapper.StudyRecordMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 培训档案与认证（EL-CUL-009 员工文化培训档案）：学习记录/考试成绩/补考记录/证书/合规状态。 */
@Service
public class ArchiveService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> CERT_TYPES = List.of("年度文化合格认证", "优秀文化践行者认证");

    @Autowired private StudyRecordMapper studyMapper;
    @Autowired private ExamInstanceMapper instanceMapper;
    @Autowired private CertMapper certMapper;
    @Autowired private AuditService audit;

    /** 全员档案聚合：学习完成数/考试次数/最高分/证书数/冻结状态。 */
    public List<Map<String, Object>> list(String keyword, Integer frozen) {
        List<StudyRecord> studies = studyMapper.selectList(new LambdaQueryWrapper<StudyRecord>().eq(StudyRecord::getDeleted, 0));
        List<ExamInstance> exams = instanceMapper.selectList(new LambdaQueryWrapper<ExamInstance>().eq(ExamInstance::getDeleted, 0));
        List<Cert> certs = certMapper.selectList(new LambdaQueryWrapper<Cert>().eq(Cert::getDeleted, 0));
        Map<String, Map<String, Object>> byUser = new LinkedHashMap<>();
        Set<String> depts = new LinkedHashSet<>();
        for (StudyRecord r : studies) {
            Map<String, Object> m = byUser.computeIfAbsent(r.getUserName(), k -> blank(k));
            m.put("studyTotal", (int) m.get("studyTotal") + 1);
            if ("已完成".equals(r.getStatus())) m.put("studyDone", (int) m.get("studyDone") + 1);
            if (r.getUserDept() != null && !r.getUserDept().isBlank()) {
                m.put("dept", r.getUserDept());
                depts.add(r.getUserDept());
            }
        }
        for (ExamInstance i : exams) {
            Map<String, Object> m = byUser.computeIfAbsent(i.getUserName(), k -> blank(k));
            m.put("examCount", (int) m.get("examCount") + 1);
            if (i.getScore() != null && i.getScore() > (int) m.get("bestScore")) m.put("bestScore", i.getScore());
            if (i.getAttempt() != null && i.getAttempt() > 1) m.put("retakeCount", (int) m.get("retakeCount") + 1);
            if (i.getFrozen() != null && i.getFrozen() == 1) {
                m.put("frozen", 1);
                if (i.getFrozenTime() != null && ((LocalDateTime) m.get("frozenTime")).isBefore(i.getFrozenTime())) {
                    m.put("frozenTime", i.getFrozenTime());
                }
            }
            if (i.getUserDept() != null && !i.getUserDept().isBlank() && "".equals(m.get("dept"))) {
                m.put("dept", i.getUserDept());
                depts.add(i.getUserDept());
            }
        }
        for (Cert c : certs) {
            Map<String, Object> m = byUser.computeIfAbsent(c.getUserName(), k -> blank(k));
            m.put("certCount", (int) m.get("certCount") + 1);
            if (c.getUserDept() != null && !c.getUserDept().isBlank() && "".equals(m.get("dept"))) {
                m.put("dept", c.getUserDept());
                depts.add(c.getUserDept());
            }
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> m : byUser.values()) {
            if (frozen != null && ((int) m.get("frozen")) != frozen) continue;
            if (keyword != null && !keyword.isBlank()
                    && !String.valueOf(m.get("userName")).contains(keyword)
                    && !String.valueOf(m.get("dept")).contains(keyword)) continue;
            out.add(m);
        }
        return out;
    }

    /** 个人档案详情（本人可查，管理岗可查任意人）。 */
    public Map<String, Object> detail(String userName) {
        boolean staff = CulRole.hasAny(CulRole.CULTURE_STAFF);
        if (!staff) {
            TrainingFlow.require(CurrentHeader.operator().equals(userName), "01", "仅可查看本人培训档案，如需他人档案请联系文化管理岗");
        }
        Map<String, Object> m = new HashMap<>();
        m.put("userName", userName);
        m.put("studyRecords", studyMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserName, userName).eq(StudyRecord::getDeleted, 0).orderByDesc(StudyRecord::getId)));
        m.put("examInstances", instanceMapper.selectList(new LambdaQueryWrapper<ExamInstance>()
                .eq(ExamInstance::getUserName, userName).eq(ExamInstance::getDeleted, 0).orderByDesc(ExamInstance::getId)));
        m.put("certs", certMapper.selectList(new LambdaQueryWrapper<Cert>()
                .eq(Cert::getUserName, userName).eq(Cert::getDeleted, 0).orderByDesc(Cert::getId)));
        long frozenCnt = instanceMapper.selectCount(new LambdaQueryWrapper<ExamInstance>()
                .eq(ExamInstance::getUserName, userName).eq(ExamInstance::getFrozen, 1).eq(ExamInstance::getDeleted, 0));
        m.put("frozen", frozenCnt > 0);
        m.put("compliance", frozenCnt > 0 ? "不合规（考试不合格，评优/晋升/调薪资格冻结）" : "合规");
        return m;
    }

    /** 手工颁发认证证书（优秀文化践行者认证由文化管理岗颁发；重复颁发拦截）。 */
    public Cert issueCert(String userName, String certType, String reason) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        TrainingFlow.require(TrainingFlow.len(userName) > 0, "01", "颁发对象必填");
        TrainingFlow.require(CERT_TYPES.contains(certType), "01", "证书类型须为 年度文化合格认证/优秀文化践行者认证");
        TrainingFlow.require(TrainingFlow.len(reason) >= 5, "13", "颁发依据/理由不少于5字");
        Long dup = certMapper.selectCount(new LambdaQueryWrapper<Cert>()
                .eq(Cert::getUserName, userName).eq(Cert::getCertType, certType).eq(Cert::getDeleted, 0));
        TrainingFlow.require(dup == 0, "13", "该人员已持有「" + certType + "」证书，请勿重复颁发");
        int year = Year.now().getValue();
        Long seq = certMapper.selectCount(new LambdaQueryWrapper<Cert>()
                .likeRight(Cert::getCertNo, "CT" + year).eq(Cert::getDeleted, 0));
        Cert c = new Cert();
        c.setCertNo("CT" + year + String.format("%04d", seq + 1));
        c.setUserName(userName);
        c.setCertType(certType);
        c.setReason(reason);
        c.setIssueDate(LocalDate.now());
        c.setIssuer(CulRole.currentRoles().stream().sorted().findFirst().orElse("C03"));
        c.setCreateTime(LocalDateTime.now());
        certMapper.insert(c);
        audit.log("CERT", "cul_cert", c.getId(), userName + " 获颁" + certType + " " + c.getCertNo());
        return c;
    }

    private Map<String, Object> blank(String userName) {
        Map<String, Object> m = new HashMap<>();
        m.put("userName", userName);
        m.put("dept", "");
        m.put("studyTotal", 0);
        m.put("studyDone", 0);
        m.put("examCount", 0);
        m.put("bestScore", 0);
        m.put("retakeCount", 0);
        m.put("certCount", 0);
        m.put("frozen", 0);
        m.put("frozenTime", LocalDateTime.MIN);
        return m;
    }
}
