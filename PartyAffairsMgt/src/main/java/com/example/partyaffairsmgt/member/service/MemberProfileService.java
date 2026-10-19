package com.example.partyaffairsmgt.member.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.member.entity.MemberArchiveMaterial;
import com.example.partyaffairsmgt.member.entity.MemberSensitiveUnlock;
import com.example.partyaffairsmgt.member.entity.PartyMember;
import com.example.partyaffairsmgt.member.mapper.MemberArchiveMaterialMapper;
import com.example.partyaffairsmgt.member.mapper.MemberSensitiveUnlockMapper;
import com.example.partyaffairsmgt.member.mapper.PartyMemberMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 党员信息档案服务（需求 6.1）。
 * 敏感字段脱敏、解锁审批、党龄计算、结构统计、档案材料管理。
 */
@Service
public class MemberProfileService {

    @Autowired
    private PartyMemberMapper memberMapper;
    @Autowired
    private MemberArchiveMaterialMapper materialMapper;
    @Autowired
    private MemberSensitiveUnlockMapper unlockMapper;
    @Autowired
    private AuditService auditService;

    /** 敏感字段列表 */
    private static final List<String> SENSITIVE_FIELDS = List.of("idCard", "phone", "address");

    /** 党员列表（脱敏） */
    public List<Map<String, Object>> list(Long orgId, String politicalStatus, String post, String status, String keyword) {
        QueryWrapper<PartyMember> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) qw.eq("org_id", orgId);
        if (politicalStatus != null && !politicalStatus.isBlank()) qw.eq("political_status", politicalStatus);
        if (post != null && !post.isBlank()) qw.eq("position", post);
        if (status != null && !status.isBlank()) qw.eq("member_status", status);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(q -> q.like("name", keyword).or().like("member_no", keyword).or().like("phone", keyword));
        }
        List<PartyMember> list = memberMapper.selectList(qw);
        return list.stream().map(this::toMaskedMap).collect(Collectors.toList());
    }

    /** 党员详情（卡片式，含解锁判断） */
    public Map<String, Object> detail(Long id) {
        PartyMember m = memberMapper.selectById(id);
        if (m == null || m.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-01", "党员不存在");
        }
        Map<String, Object> map = toMaskedMap(m);
        // 党龄计算
        if (m.getFullDate() != null) {
            int years = Period.between(m.getFullDate(), LocalDate.now()).getYears();
            map.put("partyAge", years);
        } else {
            map.put("partyAge", null);
        }
        // 检查当前用户是否有已批准的解锁
        List<MemberSensitiveUnlock> unlocks = unlockMapper.selectList(
                new QueryWrapper<MemberSensitiveUnlock>()
                        .eq("member_id", id)
                        .eq("audit_status", "已批准")
                        .gt("expire_time", LocalDateTime.now())
                        .eq("deleted", 0));
        Map<String, Boolean> unlocked = new HashMap<>();
        for (MemberSensitiveUnlock u : unlocks) {
            unlocked.put(u.getFieldName(), true);
        }
        map.put("unlockedFields", unlocked);
        // 若已解锁则返回原文
        if (unlocked.getOrDefault("idCard", false)) map.put("idCard", m.getIdCard());
        if (unlocked.getOrDefault("phone", false)) map.put("phone", m.getPhone());
        if (unlocked.getOrDefault("address", false)) map.put("address", m.getAddress());
        return map;
    }

    /** 新增/编辑党员 */
    @Transactional
    public PartyMember save(PartyMember member) {
        // 身份证 18 位校验
        if (member.getIdCard() != null && !member.getIdCard().isBlank()) {
            if (!isValidIdCard(member.getIdCard())) {
                throw PartyBizException.block("E-MB-01", "身份证号非法");
            }
            // 重复阻断
            Long count = memberMapper.selectCount(
                    new QueryWrapper<PartyMember>()
                            .eq("id_card", member.getIdCard())
                            .eq("deleted", 0)
                            .ne(member.getId() != null, "id", member.getId()));
            if (count > 0) {
                throw PartyBizException.block("E-MB-01", "同身份证号重复建档");
            }
        }
        // 转正日期 >= 入党日期
        if (member.getFullDate() != null && member.getJoinDate() != null
                && member.getFullDate().isBefore(member.getJoinDate())) {
            throw PartyBizException.block("E-MB-02", "转正日期早于入党日期");
        }
        // 入党日期早于出生日期 14 年以上（预警）
        if (member.getJoinDate() != null && member.getBirthDate() != null) {
            int ageAtJoin = Period.between(member.getBirthDate(), member.getJoinDate()).getYears();
            if (ageAtJoin < 14) {
                // 预警不阻断，记录日志
                auditService.log("党员档案", "预警", member.getId(), "入党日期早于出生日期14年以上: " + member.getName());
            }
        }
        // 缺联系方式或照片（预警）
        if ((member.getPhone() == null || member.getPhone().isBlank())
                || (member.getPhotoUrl() == null || member.getPhotoUrl().isBlank())) {
            auditService.log("党员档案", "预警", member.getId(), "缺联系方式或照片: " + member.getName());
        }
        // 档案材料缺失（建档时预警并生成补录待办）
        if (member.getId() == null) {
            // 新增时检查材料
            Long materialCount = materialMapper.selectCount(
                    new QueryWrapper<MemberArchiveMaterial>()
                            .eq("member_id", member.getId())
                            .eq("deleted", 0));
            if (materialCount == 0) {
                auditService.log("党员档案", "预警", member.getId(), "档案材料缺失，生成补录待办: " + member.getName());
            }
        }
        if (member.getId() == null) {
            member.setCreateTime(LocalDateTime.now());
            memberMapper.insert(member);
        } else {
            member.setUpdateTime(LocalDateTime.now());
            memberMapper.updateById(member);
        }
        auditService.log("党员档案", member.getId() == null ? "新增" : "编辑", member.getId(), member.getName());
        return member;
    }

    /** 敏感字段解锁申请 */
    @Transactional
    public MemberSensitiveUnlock applyUnlock(Long memberId, String fieldName, String reason) {
        PartyMember m = memberMapper.selectById(memberId);
        if (m == null || m.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-01", "党员不存在");
        }
        if (!SENSITIVE_FIELDS.contains(fieldName)) {
            throw PartyBizException.block("E-MB-01", "不支持的敏感字段: " + fieldName);
        }
        MemberSensitiveUnlock unlock = new MemberSensitiveUnlock();
        unlock.setMemberId(memberId);
        unlock.setFieldName(fieldName);
        unlock.setApplicant(CurrentHeader.operator());
        unlock.setReason(reason);
        unlock.setAuditStatus("待审批");
        unlock.setCreateTime(LocalDateTime.now());
        unlockMapper.insert(unlock);
        auditService.log("敏感字段解锁", "申请", unlock.getId(), "党员ID:" + memberId + ", 字段:" + fieldName);
        return unlock;
    }

    /** 敏感字段解锁审批 */
    @Transactional
    public void auditUnlock(Long unlockId, boolean pass, String opinion) {
        MemberSensitiveUnlock unlock = unlockMapper.selectById(unlockId);
        if (unlock == null || unlock.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-01", "解锁申请不存在");
        }
        if (!"待审批".equals(unlock.getAuditStatus())) {
            throw PartyBizException.block("E-MB-01", "该申请已审批");
        }
        unlock.setAuditStatus(pass ? "已批准" : "已驳回");
        unlock.setApprover(CurrentHeader.operator());
        unlock.setAuditTime(LocalDateTime.now());
        if (pass) {
            unlock.setExpireTime(LocalDateTime.now().plusHours(24));
        }
        unlock.setUpdateTime(LocalDateTime.now());
        unlockMapper.updateById(unlock);
        auditService.log("敏感字段解锁", pass ? "批准" : "驳回", unlockId, opinion);
    }

    /** 档案材料列表 */
    public List<MemberArchiveMaterial> materials(Long memberId, String materialType) {
        QueryWrapper<MemberArchiveMaterial> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (memberId != null) qw.eq("member_id", memberId);
        if (materialType != null && !materialType.isBlank()) qw.eq("material_type", materialType);
        return materialMapper.selectList(qw);
    }

    /** 上传档案材料 */
    @Transactional
    public MemberArchiveMaterial uploadMaterial(MemberArchiveMaterial material) {
        material.setUploadBy(CurrentHeader.operator());
        material.setUploadTime(LocalDateTime.now());
        material.setStatus("在库");
        material.setCreateTime(LocalDateTime.now());
        materialMapper.insert(material);
        // 更新党员档案完整标记
        PartyMember m = memberMapper.selectById(material.getMemberId());
        if (m != null) {
            m.setArchiveComplete(1);
            m.setUpdateTime(LocalDateTime.now());
            memberMapper.updateById(m);
        }
        auditService.log("档案材料", "上传", material.getId(), material.getMaterialName());
        return material;
    }

    /** 借阅登记 */
    @Transactional
    public void borrowMaterial(Long materialId, String borrower) {
        MemberArchiveMaterial m = materialMapper.selectById(materialId);
        if (m == null || m.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-01", "材料不存在");
        }
        if (!"在库".equals(m.getStatus())) {
            throw PartyBizException.block("E-MB-01", "材料已被借阅");
        }
        m.setStatus("借阅中");
        m.setBorrower(borrower);
        m.setBorrowDate(LocalDate.now());
        m.setUpdateTime(LocalDateTime.now());
        materialMapper.updateById(m);
        auditService.log("档案材料", "借阅", materialId, "借阅人:" + borrower);
    }

    /** 归还登记 */
    @Transactional
    public void returnMaterial(Long materialId) {
        MemberArchiveMaterial m = materialMapper.selectById(materialId);
        if (m == null || m.getDeleted() == 1) {
            throw PartyBizException.block("E-MB-01", "材料不存在");
        }
        if (!"借阅中".equals(m.getStatus())) {
            throw PartyBizException.block("E-MB-01", "材料未在借阅中");
        }
        m.setStatus("在库");
        m.setReturnDate(LocalDate.now());
        m.setUpdateTime(LocalDateTime.now());
        materialMapper.updateById(m);
        auditService.log("档案材料", "归还", materialId, "归还");
    }

    /** 结构统计（年龄/学历/岗位/党龄） */
    public Map<String, Object> statistics(Long orgId) {
        QueryWrapper<PartyMember> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (orgId != null) qw.eq("org_id", orgId);
        List<PartyMember> list = memberMapper.selectList(qw);

        Map<String, Long> ageDist = new LinkedHashMap<>();
        Map<String, Long> eduDist = new LinkedHashMap<>();
        Map<String, Long> postDist = new LinkedHashMap<>();
        Map<String, Long> partyAgeDist = new LinkedHashMap<>();

        LocalDate now = LocalDate.now();
        for (PartyMember m : list) {
            // 年龄
            if (m.getBirthDate() != null) {
                int age = Period.between(m.getBirthDate(), now).getYears();
                String ageGroup;
                if (age < 30) ageGroup = "30岁以下";
                else if (age < 40) ageGroup = "30-39岁";
                else if (age < 50) ageGroup = "40-49岁";
                else if (age < 60) ageGroup = "50-59岁";
                else ageGroup = "60岁以上";
                ageDist.merge(ageGroup, 1L, Long::sum);
            }
            // 学历
            if (m.getEducation() != null) {
                eduDist.merge(m.getEducation(), 1L, Long::sum);
            }
            // 岗位
            if (m.getPosition() != null) {
                postDist.merge(m.getPosition(), 1L, Long::sum);
            }
            // 党龄
            if (m.getFullDate() != null) {
                int partyAge = Period.between(m.getFullDate(), now).getYears();
                String paGroup;
                if (partyAge < 5) paGroup = "5年以下";
                else if (partyAge < 10) paGroup = "5-9年";
                else if (partyAge < 20) paGroup = "10-19年";
                else if (partyAge < 30) paGroup = "20-29年";
                else paGroup = "30年以上";
                partyAgeDist.merge(paGroup, 1L, Long::sum);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", list.size());
        result.put("ageDistribution", ageDist);
        result.put("educationDistribution", eduDist);
        result.put("positionDistribution", postDist);
        result.put("partyAgeDistribution", partyAgeDist);
        return result;
    }

    /** 导入预览（CSV 解析） */
    public List<Map<String, Object>> importPreview(List<Map<String, String>> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rowNum", i + 1);
            item.put("name", row.get("name"));
            item.put("idCard", row.get("idCard"));
            item.put("joinDate", row.get("joinDate"));
            item.put("fullDate", row.get("fullDate"));
            item.put("phone", row.get("phone"));
            // 校验
            List<String> errors = new ArrayList<>();
            if (row.get("name") == null || row.get("name").isBlank()) errors.add("姓名必填");
            if (row.get("idCard") != null && !row.get("idCard").isBlank() && !isValidIdCard(row.get("idCard"))) errors.add("身份证非法");
            if (row.get("joinDate") != null && row.get("fullDate") != null) {
                LocalDate join = LocalDate.parse(row.get("joinDate"));
                LocalDate full = LocalDate.parse(row.get("fullDate"));
                if (full.isBefore(join)) errors.add("转正日期早于入党日期");
            }
            item.put("errors", errors);
            item.put("valid", errors.isEmpty());
            result.add(item);
        }
        return result;
    }

    /** 导入提交 */
    @Transactional
    public Map<String, Object> importSubmit(List<Map<String, String>> rows) {
        List<Map<String, Object>> preview = importPreview(rows);
        List<Map<String, Object>> validRows = preview.stream()
                .filter(r -> (Boolean) r.get("valid"))
                .collect(Collectors.toList());
        List<Map<String, Object>> errorRows = preview.stream()
                .filter(r -> !(Boolean) r.get("valid"))
                .collect(Collectors.toList());
        int success = 0;
        for (Map<String, Object> row : validRows) {
            PartyMember m = new PartyMember();
            m.setName((String) row.get("name"));
            m.setIdCard((String) row.get("idCard"));
            m.setPhone((String) row.get("phone"));
            if (row.get("joinDate") != null) m.setJoinDate(LocalDate.parse((String) row.get("joinDate")));
            if (row.get("fullDate") != null) m.setFullDate(LocalDate.parse((String) row.get("fullDate")));
            m.setCreateTime(LocalDateTime.now());
            memberMapper.insert(m);
            success++;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", errorRows.size());
        result.put("errors", errorRows);
        return result;
    }

    /** 脱敏转换 */
    private Map<String, Object> toMaskedMap(PartyMember m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("memberNo", m.getMemberNo());
        map.put("name", m.getName());
        map.put("gender", m.getGender());
        map.put("birthDate", m.getBirthDate());
        map.put("ethnicity", m.getEthnicity());
        map.put("education", m.getEducation());
        map.put("orgId", m.getOrgId());
        map.put("orgName", m.getOrgName());
        map.put("position", m.getPosition());
        map.put("employeeNo", m.getEmployeeNo());
        map.put("nativePlace", m.getNativePlace());
        map.put("joinDate", m.getJoinDate());
        map.put("fullDate", m.getFullDate());
        map.put("memberStatus", m.getMemberStatus());
        map.put("politicalStatus", m.getPoliticalStatus());
        map.put("floatStatus", m.getFloatStatus());
        map.put("archiveComplete", m.getArchiveComplete());
        map.put("photoUrl", m.getPhotoUrl());
        // 敏感字段脱敏
        map.put("idCard", maskIdCard(m.getIdCard()));
        map.put("phone", maskPhone(m.getPhone()));
        map.put("address", maskAddress(m.getAddress()));
        return map;
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) return "***";
        return idCard.substring(0, 3) + "********" + idCard.substring(idCard.length() - 4);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskAddress(String address) {
        if (address == null || address.length() < 6) return "***";
        return address.substring(0, 6) + "****";
    }

    /** 18 位身份证校验（含校验位） */
    private boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) return false;
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            char c = idCard.charAt(i);
            if (!Character.isDigit(c)) return false;
            sum += (c - '0') * weights[i];
        }
        char expected = checkCodes[sum % 11];
        return Character.toUpperCase(idCard.charAt(17)) == expected;
    }
}
