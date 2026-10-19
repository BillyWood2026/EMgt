package com.example.skillcert.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillcert.entity.SkillPlan;

public interface SkillPlanService extends IService<SkillPlan> {

    IPage<SkillPlan> pageList(long current, long size, String planStatus, Integer planYear);

    @Override
    boolean save(SkillPlan entity);

    @Override
    boolean updateById(SkillPlan entity);

    boolean terminate(Long id);

    boolean archive(Long id);
}
