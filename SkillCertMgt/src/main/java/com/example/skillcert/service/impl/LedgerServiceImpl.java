package com.example.skillcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.skillcert.entity.SkillApplication;
import com.example.skillcert.entity.SkillCertificate;
import com.example.skillcert.service.LedgerService;
import com.example.skillcert.service.SkillApplicationService;
import com.example.skillcert.service.SkillCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LedgerServiceImpl implements LedgerService {

    @Autowired
    private SkillCertificateService certificateService;
    @Autowired
    private SkillApplicationService applicationService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<String> LEVEL_ORDER = Arrays.asList("L1", "L2", "L3", "L4", "L5");

    @Override
    public Map<String, Object> statistics() {
        List<SkillCertificate> certs = certificateService.list((Wrapper<SkillCertificate>) null);
        List<SkillApplication> apps = applicationService.list((Wrapper<SkillApplication>) null);

        long expiring = certs.stream().filter(c -> "EXPIRING".equals(c.getCertStatus())).count();
        long expired = certs.stream().filter(c -> "EXPIRED".equals(c.getCertStatus())).count();
        long pendingApply = apps.stream().filter(a -> "PENDING".equals(a.getApplyStatus())).count();
        long valid = certs.stream().filter(c -> "VALID".equals(c.getCertStatus())).count();
        long external = certs.stream().filter(c -> "EXTERNAL".equals(c.getCertType())).count();

        Map<String, Object> map = new HashMap<>();
        map.put("expiringCount", expiring);
        map.put("expiredCount", expired);
        map.put("pendingApply", pendingApply);
        map.put("totalCerts", certs.size());
        map.put("validCerts", valid);
        map.put("externalCerts", external);
        return map;
    }

    @Override
    public List<Map<String, Object>> myProfile() {
        List<SkillCertificate> certs = certificateService.list((Wrapper<SkillCertificate>) null);
        List<SkillApplication> apps = applicationService.list((Wrapper<SkillApplication>) null);

        Map<Long, List<SkillCertificate>> certGroup = certs.stream()
                .filter(c -> c.getSkillTypeId() != null)
                .collect(Collectors.groupingBy(SkillCertificate::getSkillTypeId));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<Long, List<SkillCertificate>> e : certGroup.entrySet()) {
            Long skillTypeId = e.getKey();
            List<SkillCertificate> group = e.getValue();
            SkillCertificate any = group.get(0);

            Map<String, Object> row = new HashMap<>();
            row.put("id", skillTypeId);
            row.put("skillTypeId", skillTypeId);
            row.put("skillTypeName", any.getSkillTypeName());

            List<SkillCertificate> validish = group.stream()
                    .filter(c -> "VALID".equals(c.getCertStatus()) || "EXPIRING".equals(c.getCertStatus()))
                    .collect(Collectors.toList());
            String highest = validish.stream()
                    .map(SkillCertificate::getCertLevel)
                    .filter(Objects::nonNull)
                    .max(Comparator.comparingInt(l -> {
                        int idx = LEVEL_ORDER.indexOf(l);
                        return idx < 0 ? -1 : idx;
                    }))
                    .orElse(null);
            row.put("highestLevel", highest);
            row.put("highestLevelName", levelName(highest));

            row.put("certCount", group.size());
            row.put("latestCertDate", group.stream()
                    .map(SkillCertificate::getIssueDate)
                    .filter(Objects::nonNull)
                    .max(String::compareTo)
                    .orElse(null));
            row.put("validCertCount", group.stream().filter(c -> "VALID".equals(c.getCertStatus())).count());

            Integer minDays = validish.stream()
                    .map(c -> daysToExpiry(c))
                    .filter(Objects::nonNull)
                    .filter(d -> d >= 0)
                    .min(Integer::compareTo)
                    .orElse(null);
            row.put("expiringDays", minDays);

            List<SkillApplication> myApps = apps.stream()
                    .filter(a -> skillTypeId.equals(a.getSkillTypeId()))
                    .collect(Collectors.toList());
            row.put("applyCount", myApps.size());
            row.put("passCount", myApps.stream().filter(a -> "PASS".equals(a.getResult())).count());
            row.put("workYears", myApps.stream().map(SkillApplication::getWorkYears)
                    .filter(Objects::nonNull).max(Integer::compareTo).orElse(0));
            int totalHours = myApps.stream().map(SkillApplication::getTrainHours)
                    .filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
            row.put("totalTrainHours", totalHours);
            int passRate = myApps.isEmpty() ? 0 :
                    (int) Math.round(myApps.stream().filter(a -> "PASS".equals(a.getResult())).count() * 100.0 / myApps.size());
            row.put("passRate", passRate);

            rows.add(row);
        }
        return rows;
    }

    @Override
    public Map<String, Object> employeeProfile(Long skillTypeId) {
        List<SkillCertificate> certs = certificateService.list(new LambdaQueryWrapper<SkillCertificate>()
                .eq(SkillCertificate::getSkillTypeId, skillTypeId)
                .orderByDesc(SkillCertificate::getId));
        for (SkillCertificate c : certs) {
            c.setExpiringDays(daysToExpiry(c));
            c.setCertStatusText(certStatusText(c.getCertStatus()));
        }

        List<SkillApplication> apps = applicationService.list(new LambdaQueryWrapper<SkillApplication>()
                .eq(SkillApplication::getSkillTypeId, skillTypeId)
                .orderByDesc(SkillApplication::getId));
        for (SkillApplication a : apps) {
            a.setApplyStatusText(applyStatusText(a.getApplyStatus()));
        }

        List<Map<String, Object>> history = new ArrayList<>();
        List<Map<String, Object>> retakes = new ArrayList<>();
        for (SkillApplication a : apps) {
            Map<String, Object> h = new HashMap<>();
            h.put("createTime", a.getCreateTime() != null ? a.getCreateTime().toString() : null);
            h.put("result", a.getResult());
            h.put("resultText", resultText(a.getResult()));
            h.put("applyLevel", a.getApplyLevel());
            h.put("planName", a.getPlanName());
            h.put("theoryScore", a.getTheoryScore());
            h.put("practiceScore", a.getPracticeScore());
            h.put("qualityScore", a.getQualityScore());
            h.put("finalScore", a.getFinalScore());
            history.add(h);

            if ("FAIL".equals(a.getResult()) || "SUPPLEMENT".equals(a.getResult())) {
                Map<String, Object> r = new HashMap<>();
                r.put("originalApplyTime", a.getCreateTime() != null ? a.getCreateTime().toString() : null);
                r.put("failReason", a.getDeductRemark());
                r.put("retakeTime", a.getReviewTime());
                r.put("retakeScore", a.getFinalScore());
                r.put("retakeResult", a.getResult());
                r.put("retakeResultText", resultText(a.getResult()));
                retakes.add(r);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("certificates", certs);
        result.put("certList", certs);
        result.put("history", history);
        result.put("historyList", history);
        result.put("retakes", retakes);
        result.put("retakeList", retakes);
        result.put("achievements", new ArrayList<>());
        result.put("achievementList", new ArrayList<>());
        return result;
    }

    private Integer daysToExpiry(SkillCertificate c) {
        if ("REVOKED".equals(c.getCertStatus())) {
            return null;
        }
        String dateStr = c.getValidUntil() != null ? c.getValidUntil() : c.getValidTo();
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            String d = dateStr.length() >= 10 ? dateStr.substring(0, 10) : dateStr;
            LocalDate valid = LocalDate.parse(d, DATE_FMT);
            return (int) ChronoUnit.DAYS.between(LocalDate.now(), valid);
        } catch (Exception e) {
            return null;
        }
    }

    private String levelName(String level) {
        if (level == null) return null;
        switch (level) {
            case "L1": return "初级工";
            case "L2": return "中级工";
            case "L3": return "高级工";
            case "L4": return "技师";
            case "L5": return "高级技师";
            default: return level;
        }
    }

    private String certStatusText(String status) {
        if (status == null) return "";
        switch (status) {
            case "VALID": return "有效";
            case "EXPIRING": return "即将到期";
            case "EXPIRED": return "已过期";
            case "REVOKED": return "已作废";
            default: return status;
        }
    }

    private String applyStatusText(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已审核";
            case "REJECTED": return "已拒绝";
            case "EXAMINED": return "已考核";
            default: return status;
        }
    }

    private String resultText(String result) {
        if (result == null) return "待补充";
        switch (result) {
            case "PASS": return "通过";
            case "FAIL": return "不通过";
            case "SUPPLEMENT": return "待补充";
            default: return result;
        }
    }
}
