package com.example.projectmgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmgt.entity.PmWbsTask;

import java.util.List;
import java.util.Map;

public interface PmWbsTaskService {

    List<PmWbsTask> list();

    IPage<PmWbsTask> page(Page<PmWbsTask> page, String status, Long projectId);

    PmWbsTask getById(Long id);

    PmWbsTask create(PmWbsTask task);

    PmWbsTask update(PmWbsTask task);

    void deleteById(Long id);

    void batchDelete(List<Long> ids);

    /** 查询某项目WBS权重汇总 {projectId,count,totalWeight} */
    Map<String, Object> weightSummary(Long projectId);
}
