package com.example.hrplanning.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.hrplanning.entity.TalentInventory;

public interface TalentInventoryService extends IService<TalentInventory> {

    IPage<TalentInventory> page(Integer pageNum, Integer pageSize, String orgLevel2, String headcountStatus);
}
