package com.example.screenmgt.screen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.screenmgt.common.BizException;
import com.example.screenmgt.screen.entity.ScreenDatasource;
import com.example.screenmgt.screen.entity.ScreenInstance;
import com.example.screenmgt.screen.entity.ScreenTemplate;
import com.example.screenmgt.screen.mapper.ScreenDatasourceMapper;
import com.example.screenmgt.screen.mapper.ScreenInstanceMapper;
import com.example.screenmgt.screen.mapper.ScreenTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ScreenInstanceService extends ServiceImpl<ScreenInstanceMapper, ScreenInstance> {

    private final ScreenTemplateMapper templateMapper;
    private final ScreenDatasourceMapper datasourceMapper;

    public ScreenInstanceService(ScreenTemplateMapper templateMapper, ScreenDatasourceMapper datasourceMapper) {
        this.templateMapper = templateMapper;
        this.datasourceMapper = datasourceMapper;
    }

    public List<ScreenInstance> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<ScreenInstance>()
                .orderByDesc(ScreenInstance::getCreateTime));
    }

    public ScreenInstance upsert(ScreenInstance s) {
        s.setUpdateTime(LocalDateTime.now());
        if (s.getId() == null) {
            s.setStatus(s.getStatus() == null ? "设计中" : s.getStatus());
            s.setCreateTime(s.getUpdateTime());
            baseMapper.insert(s);
        } else {
            baseMapper.updateById(s);
        }
        return s;
    }

    /** 发布大屏并生成访问令牌 */
    @Transactional
    public ScreenInstance publish(Long id) {
        ScreenInstance s = baseMapper.selectById(id);
        if (s == null) throw new BizException("大屏不存在");
        s.setStatus("已发布");
        s.setPublishTime(LocalDateTime.now());
        s.setAccessToken(UUID.randomUUID().toString().replace("-", ""));
        s.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(s);
        return s;
    }

    /** 大屏渲染: 返回完整配置+组件数据(模拟) */
    public Map<String, Object> render(Long id) {
        ScreenInstance s = baseMapper.selectById(id);
        if (s == null) throw new BizException("大屏不存在");
        if ("下线".equals(s.getStatus())) throw new BizException("大屏已下线");
        Map<String, Object> result = new HashMap<>();
        result.put("instance", s);
        if (s.getTemplateId() != null) {
            ScreenTemplate t = templateMapper.selectById(s.getTemplateId());
            result.put("template", t);
        }
        List<ScreenDatasource> datasources = datasourceMapper.selectList(
                new LambdaQueryWrapper<ScreenDatasource>().eq(ScreenDatasource::getInstanceId, id));
        result.put("datasources", datasources);
        // 模拟组件渲染数据
        result.put("components", List.of(
                Map.of("cid", "c1", "type", "line_chart", "refreshInterval", 60,
                        "data", Map.of("series", List.of(120, 200, 150, 260, 320))),
                Map.of("cid", "c2", "type", "number_card", "refreshInterval", 30,
                        "data", Map.of("value", 8860, "unit", "万元"))));
        result.put("theme", "dark-blue");
        return result;
    }
}
