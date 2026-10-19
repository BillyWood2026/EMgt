package com.example.skillcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillcert.entity.SkillCertificate;
import com.example.skillcert.mapper.SkillCertificateMapper;
import com.example.skillcert.service.SkillCertificateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkillCertificateServiceImpl extends ServiceImpl<SkillCertificateMapper, SkillCertificate>
        implements SkillCertificateService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 证书到期预警提前天数默认阈值（证书未挂工种时使用）。 */
    private static final int DEFAULT_WARN_DAYS = 90;

    @Transactional
    @Override
    public Map<String, Object> pageList(long current, long size, String employeeName, String skillName,
                                       String status, String certStatus, String certType,
                                       String certLevel, String keyword) {
        LambdaQueryWrapper<SkillCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(employeeName), w -> w
                .like(SkillCertificate::getEmployeeName, employeeName)
                .or().like(SkillCertificate::getUserName, employeeName))
                .and(StringUtils.hasText(skillName), w -> w
                .like(SkillCertificate::getSkillName, skillName)
                .or().like(SkillCertificate::getCertName, skillName))
                .eq(StringUtils.hasText(status), SkillCertificate::getStatus, status)
                .eq(StringUtils.hasText(certStatus), SkillCertificate::getCertStatus, certStatus)
                .eq(StringUtils.hasText(certType), SkillCertificate::getCertType, certType)
                .eq(StringUtils.hasText(certLevel), SkillCertificate::getCertLevel, certLevel)
                .and(StringUtils.hasText(keyword), w -> w
                .like(SkillCertificate::getCertNo, keyword)
                .or().like(SkillCertificate::getEmployeeName, keyword)
                .or().like(SkillCertificate::getUserName, keyword))
                .orderByDesc(SkillCertificate::getId);

        var page = this.page(new Page<>(current, size), wrapper);
        for (SkillCertificate c : page.getRecords()) {
            String stored = c.getCertStatus();
            Integer days = computeExpiringDays(c);
            c.setExpiringDays(days);
            // 有效期状态动态派生：已作废(REVOKED)保持；否则按有效期日期判定 EXPIRED/EXPIRING/VALID。
            String derived = deriveCertStatus(stored, days);
            if (derived != null) {
                // 派生状态与 DB 存储值不同则回写 cert_status 列，使前端按 certStatus 筛选可命中。
                if (!derived.equals(stored)) {
                    SkillCertificate up = new SkillCertificate();
                    up.setId(c.getId());
                    up.setCertStatus(derived);
                    up.setVersion(c.getVersion());
                    this.updateById(up);
                }
                c.setCertStatus(derived);
            }
            c.setCertStatusText(certStatusText(c.getCertStatus()));
        }

        // statistics 统计直接按日期谓词统计，口径与派生逻辑一致，不依赖回写。
        String todayStr = LocalDate.now().toString();
        String warnStr = LocalDate.now().plusDays(DEFAULT_WARN_DAYS).toString();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCerts", page.getTotal());
        stats.put("validCerts", count(new LambdaQueryWrapper<SkillCertificate>()
                .ne(SkillCertificate::getCertStatus, "REVOKED")
                .apply("IFNULL(valid_until, valid_to) > {0}", warnStr)));
        stats.put("expiringCerts", count(new LambdaQueryWrapper<SkillCertificate>()
                .ne(SkillCertificate::getCertStatus, "REVOKED")
                .apply("IFNULL(valid_until, valid_to) >= {0}", todayStr)
                .apply("IFNULL(valid_until, valid_to) <= {0}", warnStr)));
        stats.put("expiredCerts", count(new LambdaQueryWrapper<SkillCertificate>()
                .ne(SkillCertificate::getCertStatus, "REVOKED")
                .apply("IFNULL(valid_until, valid_to) < {0}", todayStr)));
        stats.put("externalCerts", count(new LambdaQueryWrapper<SkillCertificate>()
                .eq(SkillCertificate::getCertType, "EXTERNAL")));

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        result.put("stats", stats);
        return result;
    }

    private Integer computeExpiringDays(SkillCertificate c) {
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
            long days = ChronoUnit.DAYS.between(LocalDate.now(), valid);
            return (int) days;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 按有效期剩余天数动态派生证书状态；REVOKED 或无法计算日期时返回 null 表示保持原状态。
     */
    private String deriveCertStatus(String stored, Integer days) {
        if ("REVOKED".equals(stored) || days == null) {
            return null;
        }
        if (days < 0) {
            return "EXPIRED";
        }
        if (days <= DEFAULT_WARN_DAYS) {
            return "EXPIRING";
        }
        return "VALID";
    }

    private String certStatusText(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "VALID": return "有效";
            case "EXPIRING": return "即将到期";
            case "EXPIRED": return "已过期";
            case "REVOKED": return "已作废";
            default: return status;
        }
    }

    @Override
    public boolean revoke(Long id) {
        SkillCertificate c = this.getById(id);
        if (c == null) {
            return false;
        }
        c.setCertStatus("REVOKED");
        c.setStatus("failed");
        return this.updateById(c);
    }

    @Override
    public boolean approve(Long id) {
        SkillCertificate c = this.getById(id);
        if (c == null) {
            return false;
        }
        c.setStatus("passed");
        c.setCertStatus("VALID");
        return this.updateById(c);
    }

    @Override
    public boolean reject(Long id) {
        SkillCertificate c = this.getById(id);
        if (c == null) {
            return false;
        }
        c.setStatus("failed");
        c.setCertStatus("REVOKED");
        return this.updateById(c);
    }
}
