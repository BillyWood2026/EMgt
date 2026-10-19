package com.example.projectmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmgt.entity.ImprovementProject;
import com.example.projectmgt.mapper.ImprovementProjectMapper;
import com.example.projectmgt.service.ImprovementProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ImprovementProjectServiceImpl implements ImprovementProjectService {

    @Autowired
    private ImprovementProjectMapper projectMapper;

    @Override
    public List<ImprovementProject> list() {
        return projectMapper.selectList(null);
    }

    @Override
    public IPage<ImprovementProject> page(Page<ImprovementProject> page, String status, String type, String department) {
        LambdaQueryWrapper<ImprovementProject> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ImprovementProject::getStatus, status);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(ImprovementProject::getType, type);
        }
        if (department != null && !department.isEmpty()) {
            wrapper.eq(ImprovementProject::getDepartment, department);
        }
        wrapper.orderByDesc(ImprovementProject::getCreatedTime);
        return projectMapper.selectPage(page, wrapper);
    }

    @Override
    public ImprovementProject getById(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    @Transactional
    public ImprovementProject create(ImprovementProject project) {
        project.setCreatedTime(LocalDateTime.now());
        project.setUpdatedTime(LocalDateTime.now());
        project.setDeleted(0);
        project.setVersion(1L);
        projectMapper.insert(project);
        return project;
    }

    @Override
    @Transactional
    public ImprovementProject update(ImprovementProject project) {
        project.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(project);
        return project;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        projectMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        projectMapper.deleteBatchIds(ids);
    }
}
