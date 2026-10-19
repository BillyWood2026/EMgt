package com.example.hrplanning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.hrplanning.entity.BiOperationStat;

import java.util.List;

public interface BiOperationStatService extends IService<BiOperationStat> {

    /**
     * 按统计周期(如 2026-H1)查询 BI 经营视图行；无数据时回退为全部确定性演示数据。
     */
    List<BiOperationStat> listByPeriod(String period);
}
