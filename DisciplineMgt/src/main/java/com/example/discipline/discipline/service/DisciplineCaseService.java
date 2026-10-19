package com.example.discipline.discipline.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.discipline.discipline.entity.DisciplineCase;
import com.example.discipline.discipline.mapper.DisciplineCaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DisciplineCaseService {

    @Autowired
    private DisciplineCaseMapper caseMapper;

    public Page<DisciplineCase> page(String caseNo, String caseType, String caseLevel, String status,
                                     Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<DisciplineCase> qw = new LambdaQueryWrapper<>();
        if (caseNo != null && !caseNo.isEmpty()) qw.like(DisciplineCase::getCaseNo, caseNo);
        if (caseType != null && !caseType.isEmpty()) qw.eq(DisciplineCase::getCaseType, caseType);
        if (caseLevel != null && !caseLevel.isEmpty()) qw.eq(DisciplineCase::getCaseLevel, caseLevel);
        if (status != null && !status.isEmpty()) qw.eq(DisciplineCase::getStatus, status);
        qw.orderByDesc(DisciplineCase::getId);
        return caseMapper.selectPage(new Page<>(pageNum, pageSize), qw);
    }

    public DisciplineCase getById(Long id) {
        return caseMapper.selectById(id);
    }

    public DisciplineCase create(DisciplineCase c) {
        if (c.getCaseNo() == null || c.getCaseNo().isEmpty()) {
            c.setCaseNo("DC" + System.currentTimeMillis());
        }
        if (c.getStatus() == null || c.getStatus().isEmpty()) {
            // 线索登记为案件状态机入口
            c.setStatus("registered");
        }
        c.setId(null);
        c.setDeleted(null);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        caseMapper.insert(c);
        return c;
    }

    public DisciplineCase update(Long id, DisciplineCase c) {
        c.setId(id);
        c.setUpdateTime(LocalDateTime.now());
        caseMapper.updateById(c);
        return caseMapper.selectById(id);
    }

    /**
     * 案件状态机：线索登记 registered -> 初步核实 verified -> 立案 filed
     * -> 调查取证 investigating -> 审理 reviewing -> 处分决定 decided -> 结案 closed。
     * 仅允许相邻状态顺序推进，跨阶段/逆向跳转一律拒绝。
     */
    private static final java.util.List<String> STAGE_ORDER =
            java.util.Arrays.asList("registered", "verified", "filed", "investigating", "reviewing", "decided", "closed");

    public DisciplineCase transit(Long id, String action, String expected, String target,
                                  String handleResult, String description) {
        DisciplineCase c = caseMapper.selectById(id);
        if (c == null) {
            throw new IllegalStateException("纪检案件不存在");
        }
        if (!expected.equals(c.getStatus())) {
            throw new IllegalStateException("非法状态流转：[" + c.getStatus() + "]不能执行[" + action
                    + "]，当前须处于[" + expected + "]阶段");
        }
        if (handleResult != null) {
            c.setHandleResult(handleResult);
        }
        if (description != null) {
            c.setDescription(description);
        }
        c.setStatus(target);
        c.setUpdateTime(LocalDateTime.now());
        caseMapper.updateById(c);
        return c;
    }

    public DisciplineCase verify(Long id, DisciplineCase body) {
        return transit(id, "初步核实", "registered", "verified",
                body == null ? null : body.getHandleResult(), body == null ? null : body.getDescription());
    }

    public DisciplineCase file(Long id, DisciplineCase body) {
        return transit(id, "立案", "verified", "filed",
                body == null ? null : body.getHandleResult(), body == null ? null : body.getDescription());
    }

    public DisciplineCase investigate(Long id, DisciplineCase body) {
        return transit(id, "调查取证", "filed", "investigating",
                body == null ? null : body.getHandleResult(), body == null ? null : body.getDescription());
    }

    public DisciplineCase review(Long id, DisciplineCase body) {
        return transit(id, "审理", "investigating", "reviewing",
                body == null ? null : body.getHandleResult(), body == null ? null : body.getDescription());
    }

    public DisciplineCase decide(Long id, DisciplineCase body) {
        String result = body == null ? null : body.getHandleResult();
        if (result == null || result.trim().isEmpty()) {
            throw new IllegalStateException("作出处分决定必须填写处分/处置结果");
        }
        return transit(id, "处分决定", "reviewing", "decided", result,
                body == null ? null : body.getDescription());
    }

    public DisciplineCase close(Long id, DisciplineCase body) {
        return transit(id, "结案", "decided", "closed",
                body == null ? null : body.getHandleResult(), body == null ? null : body.getDescription());
    }

    public boolean delete(Long id) {
        return caseMapper.deleteById(id) > 0;
    }
}
