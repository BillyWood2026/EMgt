package com.example.bimgt.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bimgt.bi.entity.BiDataset;
import com.example.bimgt.bi.entity.BiReport;
import com.example.bimgt.bi.mapper.BiDatasetMapper;
import com.example.bimgt.bi.mapper.BiReportMapper;
import com.example.bimgt.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BiReportService extends ServiceImpl<BiReportMapper, BiReport> {

    private final BiDatasetMapper datasetMapper;

    public BiReportService(BiDatasetMapper datasetMapper) {
        this.datasetMapper = datasetMapper;
    }

    public List<BiReport> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<BiReport>()
                .orderByDesc(BiReport::getCreateTime));
    }

    @Transactional
    public BiReport upsert(BiReport r) {
        r.setUpdateTime(LocalDateTime.now());
        if (r.getId() == null) {
            if (r.getReportCode() == null) {
                r.setReportCode("RPT-" + System.currentTimeMillis() % 100000);
            }
            r.setStatus(r.getStatus() == null ? "草稿" : r.getStatus());
            r.setCreateTime(r.getUpdateTime());
            baseMapper.insert(r);
        } else {
            baseMapper.updateById(r);
        }
        return r;
    }

    /** 发布报表 */
    @Transactional
    public BiReport publish(Long id) {
        BiReport r = baseMapper.selectById(id);
        if (r == null) throw new BizException("报表不存在");
        r.setStatus("已发布");
        r.setPublishTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(r);
        return r;
    }

    /** 报表预览: 返回报表定义+模拟数据 */
    public Map<String, Object> preview(Long id) {
        BiReport r = baseMapper.selectById(id);
        if (r == null) throw new BizException("报表不存在");
        Map<String, Object> result = new HashMap<>();
        result.put("report", r);
        if (r.getDatasetId() != null) {
            BiDataset ds = datasetMapper.selectById(r.getDatasetId());
            result.put("dataset", ds);
        }
        // 模拟数据
        Map<String, Object> mock = new HashMap<>();
        mock.put("columns", List.of("部门", "指标值", "同比"));
        mock.put("rows", List.of(
                Map.of("部门", "研发部", "指标值", 1200, "同比", "12.5%"),
                Map.of("部门", "市场部", "指标值", 980, "同比", "-3.2%"),
                Map.of("部门", "生产部", "指标值", 2350, "同比", "8.1%")));
        mock.put("total", 3);
        result.put("mockData", mock);
        return result;
    }
}
