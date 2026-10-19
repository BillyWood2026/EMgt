package com.example.hrplanning.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.hrplanning.entity.DemandForecast;

public interface DemandForecastService extends IService<DemandForecast> {

    IPage<DemandForecast> page(Integer pageNum, Integer pageSize, String orgCode, String planNo);
}
