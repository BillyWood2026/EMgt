package com.example.partyaffairsmgt.assessment.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.assessment.entity.AssessRectify;
import com.example.partyaffairsmgt.assessment.entity.AssessTask;
import com.example.partyaffairsmgt.assessment.mapper.AssessRectifyMapper;
import com.example.partyaffairsmgt.assessment.mapper.AssessTaskMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 整改台账服务（需求 10.5）：生成整改问题清单并跟踪销号；
 * 超期未销号预警；佐证材料缺失阻断销号。
 */
@Service
public class AssessRectifyService {

    @Autowired
    private AssessRectifyMapper rectifyMapper;
    @Autowired
    private AssessTaskMapper taskMapper;
    @Autowired
    private AuditService auditService;

    public List<AssessRectify> list(Long taskId, String status) {
        QueryWrapper<AssessRectify> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (taskId != null) qw.eq("task_id", taskId);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        qw.orderByDesc("id");
        return rectifyMapper.selectList(qw);
    }

    /** 新增整改问题（任务进入整改中）。 */
    @Transactional
    public AssessRectify add(AssessRectify r) {
        AssessTask t = requireTask(r.getTaskId());
        if (r.getProblem() == null || r.getProblem().isBlank()) {
            throw PartyBizException.block("E-AS-11", "问题描述为必填项");
        }
        r.setStatus("整改中");
        r.setCreateTime(LocalDateTime.now());
        rectifyMapper.insert(r);
        if ("已发布".equals(t.getStatus())) {
            t.setStatus("整改中");
            t.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(t);
        }
        auditService.log("党建考核", "新增整改问题", r.getId(), t.getOrgName() + "/" + r.getProblem());
        return r;
    }

    /** 整改反馈（上传佐证）。 */
    @Transactional
    public AssessRectify finish(Long id, String proof) {
        AssessRectify r = requireRectify(id);
        requireStatusOpen(r);
        r.setFinishProof(proof);
        rectifyMapper.updateById(r);
        return r;
    }

    /** 复核销号：佐证材料缺失阻断；全部销号后任务进入已闭环。 */
    @Transactional
    public AssessRectify close(Long id, String closeBy) {
        AssessRectify r = requireRectify(id);
        requireStatusOpen(r);
        if (r.getFinishProof() == null || r.getFinishProof().isBlank()) {
            throw PartyBizException.block("E-AS-11", "销号须先上传整改佐证材料，佐证缺失阻断销号");
        }
        r.setStatus("已销号");
        r.setCloseTime(LocalDateTime.now());
        r.setCloseBy(closeBy);
        rectifyMapper.updateById(r);
        // 该任务下全部销号 → 已闭环
        Long open = rectifyMapper.selectCount(new QueryWrapper<AssessRectify>()
                .eq("deleted", 0).eq("task_id", r.getTaskId()).eq("status", "整改中"));
        if (open == 0) {
            AssessTask t = taskMapper.selectById(r.getTaskId());
            if (t != null && "整改中".equals(t.getStatus())) {
                t.setStatus("已闭环");
                t.setUpdateTime(LocalDateTime.now());
                taskMapper.updateById(t);
            }
        }
        auditService.log("党建考核", "整改销号", id, closeBy);
        return r;
    }

    /** 超期未销号检查（预警）。 */
    public List<Map<String, Object>> overdueCheck() {
        List<AssessRectify> open = rectifyMapper.selectList(new QueryWrapper<AssessRectify>()
                .eq("deleted", 0).eq("status", "整改中"));
        List<Map<String, Object>> overdue = new ArrayList<>();
        for (AssessRectify r : open) {
            if (r.getDeadline() != null && LocalDate.now().isAfter(r.getDeadline())) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", r.getId());
                m.put("taskId", r.getTaskId());
                m.put("problem", r.getProblem());
                m.put("deadline", r.getDeadline());
                overdue.add(m);
            }
        }
        if (!overdue.isEmpty()) {
            throw new PartyBizException("E-AS-07", PartyBizException.WARN,
                    "存在" + overdue.size() + "条整改问题超期未销号：" + overdue);
        }
        return overdue;
    }

    private AssessTask requireTask(Long taskId) {
        AssessTask t = taskMapper.selectById(taskId);
        if (t == null || t.getDeleted() == 1) throw PartyBizException.block("E-AS-11", "考核任务不存在");
        return t;
    }

    private AssessRectify requireRectify(Long id) {
        AssessRectify r = rectifyMapper.selectById(id);
        if (r == null || r.getDeleted() == 1) throw PartyBizException.block("E-AS-11", "整改记录不存在");
        return r;
    }

    private void requireStatusOpen(AssessRectify r) {
        if (!"整改中".equals(r.getStatus())) {
            throw PartyBizException.block("E-AS-11", "该问题已销号，不能重复操作");
        }
    }
}
