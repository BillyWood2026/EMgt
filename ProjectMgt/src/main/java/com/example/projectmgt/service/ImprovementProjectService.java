package com.example.projectmgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmgt.entity.ImprovementProject;

import java.util.List;

public interface ImprovementProjectService {

    List<ImprovementProject> list();

    IPage<ImprovementProject> page(Page<ImprovementProject> page, String status, String type, String department);

    ImprovementProject getById(Long id);

    ImprovementProject create(ImprovementProject project);

    ImprovementProject update(ImprovementProject project);

    void deleteById(Long id);

    void batchDelete(List<Long> ids);
}
