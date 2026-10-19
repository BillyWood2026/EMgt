package com.example.skillcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillcert.entity.SkillApplication;
import com.example.skillcert.mapper.SkillApplicationMapper;
import com.example.skillcert.service.SkillApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SkillApplicationServiceImpl extends ServiceImpl<SkillApplicationMapper, SkillApplication>
        implements SkillApplicationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public IPage<SkillApplication> pageApply(long current, long size) {
        LambdaQueryWrapper<SkillApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SkillApplication::getId);
        IPage<SkillApplication> page = this.page(new Page<>(current, size), wrapper);
        for (SkillApplication a : page.getRecords()) {
            a.setApplyStatusText(applyStatusText(a.getApplyStatus()));
        }
        return page;
    }

    @Override
    public IPage<SkillApplication> pageReview(long current, long size, String reviewStatus, Long planId) {
        LambdaQueryWrapper<SkillApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(reviewStatus), SkillApplication::getReviewStatus, reviewStatus)
                .eq(planId != null, SkillApplication::getPlanId, planId)
                .orderByDesc(SkillApplication::getId);
        IPage<SkillApplication> page = this.page(new Page<>(current, size), wrapper);
        for (SkillApplication a : page.getRecords()) {
            a.setApplyStatusText(applyStatusText(a.getApplyStatus()));
            a.setReviewStatusText(reviewStatusText(a.getReviewStatus()));
        }
        return page;
    }

    @Override
    public boolean submitApplication(SkillApplication entity) {
        if (entity.getApplyStatus() == null) {
            entity.setApplyStatus("PENDING");
        }
        if (entity.getReviewStatus() == null) {
            entity.setReviewStatus("PENDING");
        }
        if (entity.getHasExternalCert() == null) {
            entity.setHasExternalCert(0);
        }
        return this.save(entity);
    }

    @Override
    public boolean retake(Long id) {
        SkillApplication a = this.getById(id);
        if (a == null) {
            return false;
        }
        a.setReviewStatus("PENDING");
        a.setResult("SUPPLEMENT");
        return this.updateById(a);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean score(Map<String, Object> body) {
        Object idObj = body.get("id");
        if (idObj == null) {
            return false;
        }
        Long id = Long.valueOf(String.valueOf(idObj));
        SkillApplication a = this.getById(id);
        if (a == null) {
            return false;
        }

        // 实操得分：practiceScores 数组各项 score 求和
        double practiceTotal = 0;
        Object practiceScores = body.get("practiceScores");
        if (practiceScores instanceof List) {
            for (Object item : (List<Object>) practiceScores) {
                if (item instanceof Map) {
                    Object s = ((Map<String, Object>) item).get("score");
                    if (s != null) {
                        practiceTotal += toDouble(s);
                    }
                }
            }
        }

        double attendance = toDouble(body.get("attendanceScore"));
        double standard = toDouble(body.get("standardScore"));
        double mentoring = toDouble(body.get("mentoringScore"));
        double performance = toDouble(body.get("performanceScore"));
        double compliance = toDouble(body.get("complianceScore"));
        double qualityAvg = (attendance + standard + mentoring + performance + compliance) / 5.0;

        double theory = (a.getHasExternalCert() != null && a.getHasExternalCert() == 1)
                ? 100.0
                : (a.getTheoryScore() != null ? a.getTheoryScore() : 0.0);
        double finalScore = Math.round((theory * 0.4 + practiceTotal * 0.5 + qualityAvg * 0.1) * 100.0) / 100.0;

        String conclusion = (String) body.get("conclusion");
        a.setPracticeScore(practiceTotal);
        a.setQualityScore(Math.round(qualityAvg * 100.0) / 100.0);
        a.setFinalScore(finalScore);
        if (conclusion != null) {
            a.setResult(conclusion);
            a.setConclusion(conclusion);
        }
        a.setDeductRemark((String) body.get("deductRemark"));
        Object reviewerSign = body.get("reviewerSign");
        if (reviewerSign != null) {
            a.setReviewerSign(String.valueOf(reviewerSign));
            a.setReviewerName(String.valueOf(reviewerSign));
        }
        a.setReviewTime(LocalDateTime.now().format(FMT));
        a.setReviewStatus("SCORED");
        if ("PASS".equals(conclusion)) {
            a.setReviewStatus("COMPLETED");
            a.setApplyStatus("EXAMINED");
        }
        return this.updateById(a);
    }

    private double toDouble(Object o) {
        if (o == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String applyStatusText(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已审核";
            case "REJECTED": return "已拒绝";
            case "EXAMINED": return "已考核";
            default: return status;
        }
    }

    private String reviewStatusText(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "PENDING": return "待评审";
            case "SCORED": return "已评分";
            case "COMPLETED": return "已完成";
            case "RETAKE": return "补考中";
            default: return status;
        }
    }
}
