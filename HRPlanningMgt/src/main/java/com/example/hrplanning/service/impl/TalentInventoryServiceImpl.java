package com.example.hrplanning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.hrplanning.entity.TalentInventory;
import com.example.hrplanning.mapper.TalentInventoryMapper;
import com.example.hrplanning.service.TalentInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TalentInventoryServiceImpl
        extends ServiceImpl<TalentInventoryMapper, TalentInventory>
        implements TalentInventoryService {

    @Override
    public IPage<TalentInventory> page(Integer pageNum, Integer pageSize, String orgLevel2, String headcountStatus) {
        LambdaQueryWrapper<TalentInventory> wrapper = new LambdaQueryWrapper<TalentInventory>()
                .eq(StringUtils.hasText(orgLevel2), TalentInventory::getOrgLevel2, orgLevel2)
                .eq(StringUtils.hasText(headcountStatus), TalentInventory::getHeadcountStatus, headcountStatus)
                .orderByDesc(TalentInventory::getId);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
