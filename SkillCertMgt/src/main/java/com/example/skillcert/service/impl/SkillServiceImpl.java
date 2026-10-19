package com.example.skillcert.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillcert.entity.Skill;
import com.example.skillcert.mapper.SkillMapper;
import com.example.skillcert.service.SkillService;
import org.springframework.stereotype.Service;

@Service
public class SkillServiceImpl extends ServiceImpl<SkillMapper, Skill> implements SkillService {

    @Override
    public IPage<Skill> pageList(long current, long size) {
        return this.page(new Page<>(current, size), null);
    }
}
