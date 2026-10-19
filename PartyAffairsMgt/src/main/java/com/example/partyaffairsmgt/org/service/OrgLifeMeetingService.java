package com.example.partyaffairsmgt.org.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.org.entity.MeetingAbsence;
import com.example.partyaffairsmgt.org.entity.MeetingMaterial;
import com.example.partyaffairsmgt.org.entity.MeetingRectification;
import com.example.partyaffairsmgt.org.entity.OrgLifeMeeting;
import com.example.partyaffairsmgt.org.entity.PartyOrg;
import com.example.partyaffairsmgt.org.mapper.MeetingAbsenceMapper;
import com.example.partyaffairsmgt.org.mapper.MeetingMaterialMapper;
import com.example.partyaffairsmgt.org.mapper.MeetingRectificationMapper;
import com.example.partyaffairsmgt.org.mapper.OrgLifeMeetingMapper;
import com.example.partyaffairsmgt.org.mapper.PartyOrgMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 4.3 组织生活会/民主生活会：发起补录、材料齐备校验、整改台账与销号、频次统计。 */
@Service
public class OrgLifeMeetingService {

    public static final List<String> REQUIRED_MATERIALS =
            List.of("方案", "学习记录", "征求意见汇总", "谈心谈话记录");

    @Autowired private OrgLifeMeetingMapper meetingMapper;
    @Autowired private MeetingMaterialMapper materialMapper;
    @Autowired private MeetingAbsenceMapper absenceMapper;
    @Autowired private MeetingRectificationMapper rectificationMapper;
    @Autowired private PartyOrgMapper orgMapper;
    @Autowired private AuditService auditService;

    public List<OrgLifeMeeting> list(Long orgId, String meetingType, Integer year, String status) {
        QueryWrapper<OrgLifeMeeting> qw = new QueryWrapper<>();
        if (orgId != null) {
            qw.eq("org_id", orgId);
        }
        if (meetingType != null && !meetingType.isBlank()) {
            qw.eq("meeting_type", meetingType);
        }
        if (status != null && !status.isBlank()) {
            qw.eq("status", status);
        }
        if (year != null) {
            qw.apply("year(meeting_date)={0}", year);
        }
        qw.orderByDesc("meeting_date", "id");
        return meetingMapper.selectList(qw);
    }

    @Transactional
    public OrgLifeMeeting create(OrgLifeMeeting m) {
        validateBase(m);
        if (m.getMeetingType() == null || !Arrays.asList("民主生活会", "组织生活会").contains(m.getMeetingType())) {
            throw PartyBizException.block("E-MEET-01", "会议类型必须为民主生活会/组织生活会");
        }
        // 事后补录必须注明原因
        if (m.getSupplement() != null && m.getSupplement() == 1
                && (m.getSupplementReason() == null || m.getSupplementReason().isBlank())) {
            throw PartyBizException.block("E-MEET-01", "记录补录必须注明补录原因");
        }
        if (m.getStatus() == null) {
            m.setStatus("计划中");
        }
        if ("已召开".equals(m.getStatus())) {
            checkReadyToHold(m);
        }
        LocalDateTime now = LocalDateTime.now();
        m.setCreateTime(now);
        m.setUpdateTime(now);
        meetingMapper.insert(m);
        auditService.log("MEETING_CREATE", "org_life_meeting", m.getId(),
                m.getMeetingType() + " " + (m.getTitle() == null ? m.getTopic() : m.getTitle()));
        return m;
    }

    @Transactional
    public OrgLifeMeeting update(OrgLifeMeeting m) {
        OrgLifeMeeting db = mustGet(m.getId());
        if (m.getSupplement() != null && m.getSupplement() == 1
                && (m.getSupplementReason() == null || m.getSupplementReason().isBlank())) {
            throw PartyBizException.block("E-MEET-01", "记录补录必须注明补录原因");
        }
        m.setUpdateTime(LocalDateTime.now());
        meetingMapper.updateById(m);
        return meetingMapper.selectById(m.getId());
    }

    private void validateBase(OrgLifeMeeting m) {
        if (m.getTopic() == null || m.getTopic().isBlank()) {
            throw PartyBizException.block("E-MEET-01", "会议主题必填");
        }
        if (m.getTopic().length() > 100) {
            throw PartyBizException.block("E-MEET-01", "会议主题不超过100字");
        }
        if (m.getMeetingDate() == null) {
            throw PartyBizException.block("E-MEET-01", "会议时间必填");
        }
        if (m.getHost() == null || m.getHost().isBlank()) {
            throw PartyBizException.block("E-MEET-01", "主持人必填");
        }
    }

    // ---------- 会前材料 ----------

    public List<MeetingMaterial> materials(Long meetingId) {
        return materialMapper.selectList(new QueryWrapper<MeetingMaterial>()
                .eq("meeting_id", meetingId).orderByAsc("material_type", "id"));
    }

    @Transactional
    public List<MeetingMaterial> saveMaterials(Long meetingId, List<MeetingMaterial> materials) {
        mustGet(meetingId);
        materialMapper.delete(new QueryWrapper<MeetingMaterial>().eq("meeting_id", meetingId));
        List<MeetingMaterial> saved = new ArrayList<>();
        for (MeetingMaterial mm : materials) {
            if (mm.getMaterialType() == null || !REQUIRED_MATERIALS.contains(mm.getMaterialType())) {
                throw PartyBizException.block("E-MEET-02", "材料类型非法：" + mm.getMaterialType());
            }
            mm.setId(null);
            mm.setMeetingId(meetingId);
            LocalDateTime now = LocalDateTime.now();
            mm.setUploadTime(now);
            mm.setCreateTime(now);
            mm.setUpdateTime(now);
            materialMapper.insert(mm);
            saved.add(mm);
        }
        return saved;
    }

    // ---------- 缺席人员 ----------

    public List<MeetingAbsence> absences(Long meetingId) {
        return absenceMapper.selectList(new QueryWrapper<MeetingAbsence>()
                .eq("meeting_id", meetingId).orderByAsc("id"));
    }

    @Transactional
    public List<MeetingAbsence> saveAbsences(Long meetingId, List<MeetingAbsence> absences) {
        mustGet(meetingId);
        absenceMapper.delete(new QueryWrapper<MeetingAbsence>().eq("meeting_id", meetingId));
        List<MeetingAbsence> saved = new ArrayList<>();
        for (MeetingAbsence a : absences) {
            if (a.getMemberName() == null || a.getMemberName().isBlank()) {
                continue;
            }
            if (a.getReason() == null || a.getReason().isBlank()) {
                throw PartyBizException.block("E-MEET-03",
                        "缺席人员 " + a.getMemberName() + " 未填写缺席原因，不能归档");
            }
            a.setId(null);
            a.setMeetingId(meetingId);
            LocalDateTime now = LocalDateTime.now();
            a.setCreateTime(now);
            a.setUpdateTime(now);
            absenceMapper.insert(a);
            saved.add(a);
        }
        return saved;
    }

    // ---------- 登记已召开 ----------

    /** 四类会前材料齐备 + 缺席均有原因，方可登记“已召开”。 */
    @Transactional
    public OrgLifeMeeting markHeld(Long meetingId) {
        OrgLifeMeeting m = mustGet(meetingId);
        checkReadyToHold(m);
        m.setStatus("已召开");
        m.setPreMaterialsReady(1);
        m.setUpdateTime(LocalDateTime.now());
        meetingMapper.updateById(m);
        auditService.log("MEETING_HELD", "org_life_meeting", meetingId, "登记已召开");
        return m;
    }

    private void checkReadyToHold(OrgLifeMeeting m) {
        Set<String> uploaded = materialMapper.selectList(new QueryWrapper<MeetingMaterial>()
                        .eq("meeting_id", m.getId())).stream()
                .map(MeetingMaterial::getMaterialType).collect(Collectors.toSet());
        List<String> missing = REQUIRED_MATERIALS.stream()
                .filter(t -> !uploaded.contains(t)).collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw PartyBizException.block("E-MEET-02",
                    "会前准备材料不齐，缺失：" + String.join("、", missing) + "，不能登记“已召开”");
        }
        long noReason = absenceMapper.selectList(new QueryWrapper<MeetingAbsence>()
                .eq("meeting_id", m.getId())).stream()
                .filter(a -> a.getReason() == null || a.getReason().isBlank()).count();
        if (noReason > 0) {
            throw PartyBizException.block("E-MEET-03", "存在未填写原因的缺席人员，不能归档");
        }
    }

    // ---------- 整改清单 / 销号 ----------

    public List<MeetingRectification> rectifications(Long meetingId) {
        List<MeetingRectification> list = rectificationMapper.selectList(new QueryWrapper<MeetingRectification>()
                .eq(meetingId != null, "meeting_id", meetingId)
                .orderByAsc("deadline"));
        list.forEach(this::refreshLevel);
        return list;
    }

    @Transactional
    public MeetingRectification saveRectification(MeetingRectification r) {
        if (r.getProblem() == null || r.getProblem().isBlank()) {
            throw PartyBizException.block("E-MEET-04", "整改问题不能为空");
        }
        if (r.getStatus() == null) {
            r.setStatus("未整改");
        }
        LocalDateTime now = LocalDateTime.now();
        if (r.getId() == null) {
            r.setCreateTime(now);
            r.setUpdateTime(now);
            rectificationMapper.insert(r);
        } else {
            r.setUpdateTime(now);
            rectificationMapper.updateById(r);
        }
        refreshLevel(r);
        return r;
    }

    @Transactional
    public MeetingRectification closeRectification(Long id, String result, String evidenceUrl) {
        MeetingRectification r = rectificationMapper.selectById(id);
        if (r == null) {
            throw PartyBizException.block("E-MEET-04", "整改记录不存在");
        }
        if (result == null || result.isBlank()) {
            throw PartyBizException.block("E-MEET-04", "销号必须填写整改结果");
        }
        r.setStatus("已销号");
        r.setResult(result);
        r.setEvidenceUrl(evidenceUrl);
        r.setCloseDate(LocalDate.now());
        r.setReminderLevel("正常");
        r.setUpdateTime(LocalDateTime.now());
        rectificationMapper.updateById(r);
        auditService.log("MEETING_RECTIFY_CLOSE", "meeting_rectification", id, "整改销号");
        return r;
    }

    /** 到期提醒：未销号项，截止前7天“临近到期”；超期“已超期”；超期30天“超期告警”。 */
    private void refreshLevel(MeetingRectification r) {
        if ("已销号".equals(r.getStatus()) || r.getDeadline() == null) {
            if (!"已销号".equals(r.getStatus())) {
                r.setReminderLevel("正常");
            }
            return;
        }
        LocalDate today = LocalDate.now();
        if (today.isAfter(r.getDeadline().plusDays(30))) {
            r.setReminderLevel("超期告警");
        } else if (today.isAfter(r.getDeadline())) {
            r.setReminderLevel("已超期");
        } else if (!today.isBefore(r.getDeadline().minusDays(7))) {
            r.setReminderLevel("临近到期");
        } else {
            r.setReminderLevel("正常");
        }
        rectificationMapper.updateById(r);
    }

    // ---------- 统计：召开率 / 按时率 / 应开未开 ----------

    public Map<String, Object> stats(Integer year) {
        int y = year == null ? LocalDate.now().getYear() : year;
        List<PartyOrg> orgs = orgMapper.selectList(new QueryWrapper<PartyOrg>()
                .in("org_type", "党委", "党总支", "党支部", "临时党支部")
                .eq("audit_status", "APPROVED").ne("status", "撤销"));
        List<Map<String, Object>> rows = new ArrayList<>();
        int quarter = (LocalDate.now().getMonthValue() - 1) / 3 + 1;
        for (PartyOrg o : orgs) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("orgId", o.getId());
            row.put("orgName", o.getName());
            Map<String, String> missAlert = new LinkedHashMap<>();
            for (String t : Arrays.asList("民主生活会", "组织生活会")) {
                long held = meetingMapper.selectCount(new QueryWrapper<OrgLifeMeeting>()
                        .eq("org_id", o.getId()).eq("meeting_type", t).eq("status", "已召开")
                        .apply("year(meeting_date)={0}", y));
                row.put(t, held);
                if (held < 1) {
                    // 季度末仍未召开升级告警，其余预警
                    missAlert.put(t, quarter >= 4 ? "告警(年末未召开，通知上级)" : "预警(应开未开)");
                }
            }
            row.put("alerts", missAlert);
            rows.add(row);
        }
        long need = orgs.size() * 2L;
        long done = rows.stream().flatMap(r -> Arrays.asList("民主生活会", "组织生活会").stream()
                .map(t -> ((Number) r.get(t)).longValue() > 0 ? 1L : 0L)).count();
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("year", y);
        r.put("orgCount", orgs.size());
        r.put("requiredSessions", need);
        r.put("heldSessions", rows.stream().flatMap(x -> Arrays.asList("民主生活会", "组织生活会").stream()
                .map(t -> ((Number) x.get(t)).longValue())).reduce(0L, Long::sum));
        r.put("holdRate", need == 0 ? 100 : Math.round(done * 1000.0 / need) / 10.0);
        r.put("details", rows);
        return r;
    }

    private OrgLifeMeeting mustGet(Long id) {
        OrgLifeMeeting m = meetingMapper.selectById(id);
        if (m == null) {
            throw PartyBizException.block("E-MEET-01", "会议记录不存在：" + id);
        }
        return m;
    }
}
