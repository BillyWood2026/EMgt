package com.example.projectmgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmgt.entity.PmProjectClosure;

import java.util.List;

public interface PmProjectClosureService {

    List<PmProjectClosure> list();

    IPage<PmProjectClosure> page(Page<PmProjectClosure> page, String status, Long projectId);

    PmProjectClosure getById(Long id);

    PmProjectClosure create(PmProjectClosure closure);

    PmProjectClosure update(PmProjectClosure closure);

    void deleteById(Long id);

    void batchDelete(List<Long> ids);

    /** pending -> reviewing */
    PmProjectClosure submit(Long id);

    /** reviewing -> passed / rejected */
    PmProjectClosure review(Long id, boolean passed, String opinion);

    /** 结项归档：passed -> archived，并联动项目归档（校验四个前置完成标志） */
    PmProjectClosure archive(Long id);
}
