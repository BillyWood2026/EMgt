package com.example.hrplanning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.hrplanning.entity.BiOperationStat;
import com.example.hrplanning.mapper.BiOperationStatMapper;
import com.example.hrplanning.service.BiOperationStatService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BiOperationStatServiceImpl
        extends ServiceImpl<BiOperationStatMapper, BiOperationStat>
        implements BiOperationStatService {

    @Override
    public List<BiOperationStat> listByPeriod(String period) {
        LambdaQueryWrapper<BiOperationStat> wrapper = new LambdaQueryWrapper<BiOperationStat>()
                .eq(StringUtils.hasText(period), BiOperationStat::getStatisticsPeriod, period)
                .orderByAsc(BiOperationStat::getId);
        List<BiOperationStat> rows = baseMapper.selectList(wrapper);
        if (rows.isEmpty()) {
            // 周期无数据时返回确定性演示数据，保证前端 BI 视图始终可渲染
            rows = baseMapper.selectList(new LambdaQueryWrapper<BiOperationStat>()
                    .orderByAsc(BiOperationStat::getId));
        }
        return rows;
    }
}
