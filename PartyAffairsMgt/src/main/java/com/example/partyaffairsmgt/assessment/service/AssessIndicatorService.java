package com.example.partyaffairsmgt.assessment.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.assessment.entity.AssessIndicator;
import com.example.partyaffairsmgt.assessment.mapper.AssessIndicatorMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 考核指标体系服务（需求 10.1）。
 * 指标编码唯一、层级 1/2/3 递进，父级必须存在；有子级的指标禁止删除；
 * 被已发布方案引用的指标禁止停用/删除。
 * 错误码：E-AS-01 指标编码重复/层级非法/存在子级阻断。
 */
@Service
public class AssessIndicatorService {

    @Autowired
    private AssessIndicatorMapper indicatorMapper;
    @Autowired
    private AuditService auditService;

    public List<AssessIndicator> list(Integer level, String targetType, String status, String keyword) {
        QueryWrapper<AssessIndicator> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (level != null) qw.eq("level", level);
        if (targetType != null && !targetType.isBlank()) qw.eq("target_type", targetType);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        if (keyword != null && !keyword.isBlank()) qw.like("indicator_name", keyword);
        qw.orderByAsc("sort_order");
        return indicatorMapper.selectList(qw);
    }

    @Transactional
    public AssessIndicator save(AssessIndicator ind) {
        if (ind.getIndicatorCode() == null || ind.getIndicatorCode().isBlank()
                || ind.getIndicatorName() == null || ind.getIndicatorName().isBlank()
                || ind.getLevel() == null) {
            throw PartyBizException.block("E-AS-01", "指标编码、名称、层级为必填项");
        }
        // 编码唯一
        QueryWrapper<AssessIndicator> qw = new QueryWrapper<AssessIndicator>()
                .eq("deleted", 0).eq("indicator_code", ind.getIndicatorCode());
        if (ind.getId() != null) qw.ne("id", ind.getId());
        if (indicatorMapper.selectCount(qw) > 0) {
            throw PartyBizException.block("E-AS-01", "指标编码[" + ind.getIndicatorCode() + "]已存在");
        }
        // 层级与父级校验
        if (ind.getLevel() < 1 || ind.getLevel() > 3) {
            throw PartyBizException.block("E-AS-01", "指标层级仅支持一级/二级/三级");
        }
        if (ind.getLevel() > 1) {
            if (ind.getParentCode() == null || ind.getParentCode().isBlank()) {
                throw PartyBizException.block("E-AS-01", "二级/三级指标必须指定父级编码");
            }
            AssessIndicator parent = indicatorMapper.selectOne(new QueryWrapper<AssessIndicator>()
                    .eq("deleted", 0).eq("indicator_code", ind.getParentCode()));
            if (parent == null || parent.getLevel() != ind.getLevel() - 1) {
                throw PartyBizException.block("E-AS-01", "父级编码[" + ind.getParentCode() + "]不存在或层级不匹配");
            }
        }
        if (ind.getVetoFlag() == null) ind.setVetoFlag(0);
        if (ind.getStatus() == null) ind.setStatus("启用");
        ind.setUpdateTime(LocalDateTime.now());
        if (ind.getId() == null) {
            ind.setCreateTime(LocalDateTime.now());
            indicatorMapper.insert(ind);
            auditService.log("党建考核", "新增指标", ind.getId(), ind.getIndicatorCode() + "/" + ind.getIndicatorName());
        } else {
            indicatorMapper.updateById(ind);
            auditService.log("党建考核", "修改指标", ind.getId(), ind.getIndicatorCode() + "/" + ind.getIndicatorName());
        }
        return ind;
    }

    /** 删除：存在子级或被方案引用时阻断（E-AS-01）。 */
    @Transactional
    public void remove(Long id) {
        AssessIndicator ind = indicatorMapper.selectById(id);
        if (ind == null) throw PartyBizException.block("E-AS-01", "指标不存在");
        Long children = indicatorMapper.selectCount(new QueryWrapper<AssessIndicator>()
                .eq("deleted", 0).eq("parent_code", ind.getIndicatorCode()));
        if (children > 0) {
            throw PartyBizException.block("E-AS-01", "该指标下存在" + children + "个子级指标，禁止删除");
        }
        // deleted 带 @TableLogic，updateById 会剔除该字段，必须走 deleteById 触发逻辑删除
        indicatorMapper.deleteById(id);
        auditService.log("党建考核", "删除指标", id, ind.getIndicatorCode() + "/" + ind.getIndicatorName());
    }
}
