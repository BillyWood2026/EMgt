package com.example.bimgt.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bimgt.bi.entity.BiSubscription;
import com.example.bimgt.bi.mapper.BiSubscriptionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BiSubscriptionService extends ServiceImpl<BiSubscriptionMapper, BiSubscription> {

    public List<BiSubscription> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<BiSubscription>()
                .orderByDesc(BiSubscription::getCreateTime));
    }

    public BiSubscription upsert(BiSubscription s) {
        s.setUpdateTime(LocalDateTime.now());
        if (s.getId() == null) {
            s.setStatus(s.getStatus() == null ? "启用" : s.getStatus());
            s.setCreateTime(s.getUpdateTime());
            baseMapper.insert(s);
        } else {
            baseMapper.updateById(s);
        }
        return s;
    }
}
