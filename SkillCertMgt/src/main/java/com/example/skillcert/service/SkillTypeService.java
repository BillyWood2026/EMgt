package com.example.skillcert.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillcert.entity.SkillType;

public interface SkillTypeService extends IService<SkillType> {

    IPage<SkillType> pageList(long current, long size, String certType, String status, String skillLevel);

    @Override
    boolean save(SkillType entity);

    @Override
    boolean updateById(SkillType entity);
}
