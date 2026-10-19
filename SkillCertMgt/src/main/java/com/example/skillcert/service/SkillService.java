package com.example.skillcert.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillcert.entity.Skill;

public interface SkillService extends IService<Skill> {

    IPage<Skill> pageList(long current, long size);
}
