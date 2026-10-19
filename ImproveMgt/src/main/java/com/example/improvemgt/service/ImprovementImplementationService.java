package com.example.improvemgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.improvemgt.entity.ImprovementImplementation;

import java.util.List;

public interface ImprovementImplementationService {

    List<ImprovementImplementation> list();

    IPage<ImprovementImplementation> page(Page<ImprovementImplementation> page, String status, String department);

    ImprovementImplementation getById(Long id);

    ImprovementImplementation create(ImprovementImplementation implementation);

    ImprovementImplementation update(ImprovementImplementation implementation);

    void deleteById(Long id);

    void batchDelete(List<Long> ids);

    /** pending -> ongoing */
    ImprovementImplementation start(Long id);

    /** ongoing -> paused */
    ImprovementImplementation pause(Long id);

    /** paused -> ongoing */
    ImprovementImplementation resume(Long id);

    /** ongoing -> completed(进度100)，联动提案 completed */
    ImprovementImplementation complete(Long id);
}
