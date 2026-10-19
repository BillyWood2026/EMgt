package com.example.strategymgt.sixs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.strategymgt.sixs.entity.*;
import com.example.strategymgt.sixs.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SixsService extends ServiceImpl<SixsS5PerformanceMapper, SixsS5Performance> {

    private final SixsS1PlanMapper s1Mapper;
    private final SixsS2BusinessMapper s2Mapper;
    private final SixsS3ReportMapper s3Mapper;
    private final SixsS4AuditMapper s4Mapper;
    private final SixsS6ManagerMapper s6Mapper;

    public SixsService(SixsS1PlanMapper s1Mapper, SixsS2BusinessMapper s2Mapper,
                       SixsS3ReportMapper s3Mapper, SixsS4AuditMapper s4Mapper,
                       SixsS6ManagerMapper s6Mapper) {
        this.s1Mapper = s1Mapper;
        this.s2Mapper = s2Mapper;
        this.s3Mapper = s3Mapper;
        this.s4Mapper = s4Mapper;
        this.s6Mapper = s6Mapper;
    }

    // ==================== S1 战略规划监控 ====================
    public List<SixsS1Plan> listS1() { return s1Mapper.selectList(null); }

    @Transactional
    public SixsS1Plan saveS1(SixsS1Plan s) {
        if (s.getId() != null) {
            s.setUpdateTime(LocalDateTime.now());
            s1Mapper.updateById(s);
            return s;
        }
        s.setCreateTime(LocalDateTime.now());
        s1Mapper.insert(s);
        return s;
    }

    public boolean updateS1(SixsS1Plan s) {
        s.setUpdateTime(LocalDateTime.now());
        return s1Mapper.updateById(s) > 0;
    }

    public boolean deleteS1(Long id) {
        return s1Mapper.deleteById(id) > 0;
    }

    // ==================== S2 商业计划监控 ====================
    public List<SixsS2Business> listS2() { return s2Mapper.selectList(null); }

    @Transactional
    public SixsS2Business saveS2(SixsS2Business s) {
        calcS2(s);
        if (s.getId() != null) {
            s.setUpdateTime(LocalDateTime.now());
            s2Mapper.updateById(s);
            return s;
        }
        s.setCreateTime(LocalDateTime.now());
        s2Mapper.insert(s);
        return s;
    }

    private void calcS2(SixsS2Business s) {
        // 完成率 = 实际/目标×100%
        if (s.getBpActual() != null && s.getBpGoal() != null && s.getBpGoal().compareTo(BigDecimal.ZERO) != 0) {
            s.setCompletionRate(s.getBpActual().divide(s.getBpGoal(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        }
        // 红绿灯预警: 绿±5%/黄5-15%/红>15%
        if (s.getCompletionRate() != null) {
            BigDecimal dev = s.getCompletionRate().subtract(new BigDecimal("100")).abs();
            if (dev.compareTo(new BigDecimal("5")) <= 0) s.setBusinessStatus("绿");
            else if (dev.compareTo(new BigDecimal("15")) <= 0) s.setBusinessStatus("黄");
            else s.setBusinessStatus("红");
        }
    }

    public boolean updateS2(SixsS2Business s) {
        calcS2(s);
        s.setUpdateTime(LocalDateTime.now());
        return s2Mapper.updateById(s) > 0;
    }

    public boolean deleteS2(Long id) {
        return s2Mapper.deleteById(id) > 0;
    }

    // ==================== S3 管理报告监控 ====================
    public List<SixsS3Report> listS3() { return s3Mapper.selectList(null); }

    @Transactional
    public SixsS3Report saveS3(SixsS3Report s) {
        if (s.getId() != null) {
            s.setUpdateTime(LocalDateTime.now());
            s3Mapper.updateById(s);
            return s;
        }
        s.setCreateTime(LocalDateTime.now());
        s3Mapper.insert(s);
        return s;
    }

    public boolean updateS3(SixsS3Report s) {
        s.setUpdateTime(LocalDateTime.now());
        return s3Mapper.updateById(s) > 0;
    }

    public boolean deleteS3(Long id) {
        return s3Mapper.deleteById(id) > 0;
    }

    // ==================== S4 内部审计监控 ====================
    public List<SixsS4Audit> listS4() { return s4Mapper.selectList(null); }

    @Transactional
    public SixsS4Audit saveS4(SixsS4Audit s) {
        if (s.getId() != null) {
            s.setUpdateTime(LocalDateTime.now());
            s4Mapper.updateById(s);
            return s;
        }
        s.setCreateTime(LocalDateTime.now());
        s4Mapper.insert(s);
        return s;
    }

    public boolean updateS4(SixsS4Audit s) {
        s.setUpdateTime(LocalDateTime.now());
        return s4Mapper.updateById(s) > 0;
    }

    public boolean deleteS4(Long id) {
        return s4Mapper.deleteById(id) > 0;
    }

    // ==================== S5 业绩评价监控 ====================
    public List<SixsS5Performance> listS5() { return baseMapper.selectList(null); }

    @Transactional
    public SixsS5Performance saveS5(SixsS5Performance s) {
        calcS5(s);
        if (s.getId() != null) {
            s.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(s);
            return s;
        }
        s.setCreateTime(LocalDateTime.now());
        baseMapper.insert(s);
        return s;
    }

    private void calcS5(SixsS5Performance s) {
        // 完成率 = 实际/目标×100%
        if (s.getActualValue() != null && s.getTargetValue() != null
                && s.getTargetValue().compareTo(BigDecimal.ZERO) != 0) {
            s.setCompletionRate(s.getActualValue().divide(s.getTargetValue(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        }
        // 业绩等级 S/A/B/C/D
        if (s.getCompletionRate() != null) {
            BigDecimal r = s.getCompletionRate();
            if (r.compareTo(new BigDecimal("120")) >= 0) s.setPerfLevel("S");
            else if (r.compareTo(new BigDecimal("100")) >= 0) s.setPerfLevel("A");
            else if (r.compareTo(new BigDecimal("90")) >= 0) s.setPerfLevel("B");
            else if (r.compareTo(new BigDecimal("80")) >= 0) s.setPerfLevel("C");
            else s.setPerfLevel("D");
        }
    }

    public boolean updateS5(SixsS5Performance s) {
        calcS5(s);
        s.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(s) > 0;
    }

    public boolean deleteS5(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    // 业绩综合得分 = ∑(同一次评价 evalCode 下维度得分×权重)
    public BigDecimal calcTotalPerformanceScore(String evalCode) {
        List<SixsS5Performance> list = baseMapper.selectList(
                new LambdaQueryWrapper<SixsS5Performance>().eq(SixsS5Performance::getEvalCode, evalCode));
        return list.stream()
                .filter(p -> p.getDimScore() != null && p.getWeight() != null)
                .map(p -> p.getDimScore().multiply(p.getWeight()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== S6 经理人考评监控 ====================
    public List<SixsS6Manager> listS6() { return s6Mapper.selectList(null); }

    @Transactional
    public SixsS6Manager saveS6(SixsS6Manager s) {
        calcS6(s);
        if (s.getId() != null) {
            s.setUpdateTime(LocalDateTime.now());
            s6Mapper.updateById(s);
            return s;
        }
        s.setCreateTime(LocalDateTime.now());
        s6Mapper.insert(s);
        return s;
    }

    private void calcS6(SixsS6Manager s) {
        // 经理人综合得分 = 单元业绩60% + 履职25% + 合规10% + 能力5%
        BigDecimal unit = s.getUnitPerfScore() == null ? BigDecimal.ZERO : s.getUnitPerfScore();
        BigDecimal duty = s.getDutyScore() == null ? BigDecimal.ZERO : s.getDutyScore();
        BigDecimal compliance = s.getComplianceScore() == null ? BigDecimal.ZERO : s.getComplianceScore();
        BigDecimal capability = s.getCapabilityScore() == null ? BigDecimal.ZERO : s.getCapabilityScore();
        s.setTotalScore(unit.multiply(new BigDecimal("0.60"))
                .add(duty.multiply(new BigDecimal("0.25")))
                .add(compliance.multiply(new BigDecimal("0.10")))
                .add(capability.multiply(new BigDecimal("0.05")))
                .setScale(2, RoundingMode.HALF_UP));
        // 业绩等级
        BigDecimal ts = s.getTotalScore();
        if (ts.compareTo(new BigDecimal("90")) >= 0) s.setPerfLevel("S");
        else if (ts.compareTo(new BigDecimal("80")) >= 0) s.setPerfLevel("A");
        else if (ts.compareTo(new BigDecimal("70")) >= 0) s.setPerfLevel("B");
        else if (ts.compareTo(new BigDecimal("60")) >= 0) s.setPerfLevel("C");
        else s.setPerfLevel("D");
    }

    public boolean updateS6(SixsS6Manager s) {
        calcS6(s);
        s.setUpdateTime(LocalDateTime.now());
        return s6Mapper.updateById(s) > 0;
    }

    public boolean deleteS6(Long id) {
        return s6Mapper.deleteById(id) > 0;
    }
}
