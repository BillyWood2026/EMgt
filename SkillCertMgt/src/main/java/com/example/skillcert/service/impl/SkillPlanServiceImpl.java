package com.example.skillcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillcert.entity.SkillPlan;
import com.example.skillcert.entity.SkillType;
import com.example.skillcert.mapper.SkillPlanMapper;
import com.example.skillcert.service.SkillPlanService;
import com.example.skillcert.service.SkillTypeService;
import com.example.skillcert.util.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillPlanServiceImpl extends ServiceImpl<SkillPlanMapper, SkillPlan>
        implements SkillPlanService {

    @Autowired
    private SkillTypeService skillTypeService;

    @Override
    public IPage<SkillPlan> pageList(long current, long size, String planStatus, Integer planYear) {
        LambdaQueryWrapper<SkillPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(planStatus), SkillPlan::getPlanStatus, planStatus)
                .eq(planYear != null, SkillPlan::getPlanYear, planYear)
                .orderByDesc(SkillPlan::getId);
        IPage<SkillPlan> page = this.page(new Page<>(current, size), wrapper);
        for (SkillPlan p : page.getRecords()) {
            fillDisplay(p);
        }
        return page;
    }

    private void fillDisplay(SkillPlan p) {
        p.setSkillTypeIds(ListUtil.splitLong(p.getSkillTypeIdsStr()));
        p.setSkillLevels(ListUtil.split(p.getSkillLevelsStr()));
        p.setSkillTypes(ListUtil.split(p.getSkillTypesStr()));
    }

    private void normalize(SkillPlan entity) {
        entity.setSkillTypeIdsStr(ListUtil.join(entity.getSkillTypeIds()));
        entity.setSkillLevelsStr(ListUtil.join(entity.getSkillLevels()));
        // 解析工种名称
        if (entity.getSkillTypeIds() != null && !entity.getSkillTypeIds().isEmpty()) {
            List<SkillType> types = skillTypeService.listByIds(entity.getSkillTypeIds());
            List<String> names = types.stream().map(SkillType::getSkillTypeName).collect(Collectors.toList());
            entity.setSkillTypesStr(ListUtil.join(names));
        } else if (entity.getSkillTypes() != null) {
            entity.setSkillTypesStr(ListUtil.join(entity.getSkillTypes()));
        }
        if (entity.getPlanStatus() == null) {
            entity.setPlanStatus("NOT_STARTED");
        }
    }

    @Override
    public boolean save(SkillPlan entity) {
        normalize(entity);
        return super.save(entity);
    }

    @Override
    public boolean updateById(SkillPlan entity) {
        normalize(entity);
        return super.updateById(entity);
    }

    @Override
    public boolean terminate(Long id) {
        SkillPlan p = this.getById(id);
        if (p == null) {
            return false;
        }
        p.setPlanStatus("ARCHIVED");
        return this.updateById(p);
    }

    @Override
    public boolean archive(Long id) {
        SkillPlan p = this.getById(id);
        if (p == null) {
            return false;
        }
        p.setPlanStatus("ARCHIVED");
        return this.updateById(p);
    }
}
