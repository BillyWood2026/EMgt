package com.example.skillcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillcert.entity.SkillType;
import com.example.skillcert.mapper.SkillTypeMapper;
import com.example.skillcert.service.SkillTypeService;
import com.example.skillcert.util.ListUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SkillTypeServiceImpl extends ServiceImpl<SkillTypeMapper, SkillType>
        implements SkillTypeService {

    @Override
    public IPage<SkillType> pageList(long current, long size, String certType, String status, String skillLevel) {
        LambdaQueryWrapper<SkillType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(certType), SkillType::getSkillCategory, certType)
                .eq(StringUtils.hasText(status), SkillType::getApplyStatus, status)
                .like(StringUtils.hasText(skillLevel), SkillType::getEnabledLevelsStr, skillLevel)
                .orderByDesc(SkillType::getId);
        IPage<SkillType> page = this.page(new Page<>(current, size), wrapper);
        for (SkillType st : page.getRecords()) {
            st.setEnabledLevels(ListUtil.split(st.getEnabledLevelsStr()));
        }
        return page;
    }

    @Override
    public boolean save(SkillType entity) {
        entity.setEnabledLevelsStr(ListUtil.join(entity.getEnabledLevels()));
        if (entity.getApplyStatus() == null) {
            entity.setApplyStatus(1);
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(SkillType entity) {
        if (entity.getEnabledLevels() != null) {
            entity.setEnabledLevelsStr(ListUtil.join(entity.getEnabledLevels()));
        }
        return super.updateById(entity);
    }
}
