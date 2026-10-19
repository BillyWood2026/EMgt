package com.example.projectmgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmgt.entity.PmProject;

import java.util.List;

public interface PmProjectService {

    List<PmProject> list();

    IPage<PmProject> page(Page<PmProject> page, String status, String type, String name);

    PmProject getById(Long id);

    PmProject create(PmProject project);

    PmProject update(PmProject project);

    void deleteById(Long id);

    void batchDelete(List<Long> ids);

    /** 重新计算预算使用率 */
    PmProject recalcBudget(Long id);

    /** 状态流转：planning -> ongoing */
    PmProject start(Long id);

    /** ongoing -> paused */
    PmProject suspend(Long id);

    /** paused -> ongoing */
    PmProject resume(Long id);

    /** ongoing -> completed */
    PmProject complete(Long id);

    /** 月结锁定 / 解锁 */
    PmProject setMonthlyLock(Long id, boolean locked);

    /** 基线冻结：校验WBS权重合计=100后保存基线版本与快照 */
    PmProject freezeBaseline(Long id);
}
