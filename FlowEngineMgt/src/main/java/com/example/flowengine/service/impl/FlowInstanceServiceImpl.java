package com.example.flowengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.flowengine.common.BizException;
import com.example.flowengine.entity.*;
import com.example.flowengine.mapper.*;
import com.example.flowengine.service.FlowInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FlowInstanceServiceImpl extends ServiceImpl<FlowInstanceMapper, FlowInstance> implements FlowInstanceService {

    private final FlowDefinitionMapper definitionMapper;
    private final FlowNodeMapper nodeMapper;
    private final FlowTaskMapper taskMapper;
    private final FlowCcMapper ccMapper;
    private final FlowCallbackLogMapper callbackLogMapper;

    public FlowInstanceServiceImpl(FlowDefinitionMapper definitionMapper, FlowNodeMapper nodeMapper,
                                   FlowTaskMapper taskMapper, FlowCcMapper ccMapper,
                                   FlowCallbackLogMapper callbackLogMapper) {
        this.definitionMapper = definitionMapper;
        this.nodeMapper = nodeMapper;
        this.taskMapper = taskMapper;
        this.ccMapper = ccMapper;
        this.callbackLogMapper = callbackLogMapper;
    }

    @Override
    @Transactional
    public FlowInstance start(FlowInstance req) {
        if (req.getDefCode() == null || req.getDefCode().isEmpty()) {
            throw new BizException("流程编码不能为空");
        }
        FlowDefinition def = definitionMapper.selectList(new LambdaQueryWrapper<FlowDefinition>()
                        .eq(FlowDefinition::getDefCode, req.getDefCode())
                        .eq(FlowDefinition::getStatus, "published")
                        .orderByDesc(FlowDefinition::getVersion))
                .stream().findFirst().orElse(null);
        if (def == null) {
            throw new BizException("流程定义不存在或未发布：" + req.getDefCode());
        }
        LocalDateTime now = LocalDateTime.now();
        FlowInstance inst = new FlowInstance();
        inst.setInstNo(nextInstNo());
        inst.setDefId(def.getId());
        inst.setDefCode(def.getDefCode());
        inst.setBizCode(req.getBizCode());
        inst.setBizId(req.getBizId());
        inst.setBizJson(req.getBizJson());
        inst.setAttachmentIds(req.getAttachmentIds());
        inst.setTitle(req.getTitle());
        inst.setInitiator(req.getInitiator());
        inst.setStatus("running");
        inst.setStartTime(now);
        inst.setCreateTime(now);
        inst.setUpdateTime(now);
        save(inst);

        List<FlowNode> nodes = nodeMapper.selectList(new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getDefId, def.getId())
                .orderByAsc(FlowNode::getSortOrder));
        boolean taskCreated = false;
        for (FlowNode node : nodes) {
            if ("cc".equals(node.getNodeType())) {
                createCc(inst.getId(), node.getApproverValue());
            } else if ("approval".equals(node.getNodeType())) {
                createTask(inst, node);
                inst.setCurrentNode(node.getNodeKey());
                inst.setUpdateTime(LocalDateTime.now());
                updateById(inst);
                taskCreated = true;
                break;
            }
        }
        if (!taskCreated) {
            throw new BizException("流程定义未配置审批节点");
        }
        return inst;
    }

    @Override
    @Transactional
    public void withdraw(Long id, String operator) {
        FlowInstance inst = getById(id);
        if (inst == null) {
            throw new BizException("流程实例不存在");
        }
        if (!"running".equals(inst.getStatus())) {
            throw new BizException("已结束的流程实例不可撤回");
        }
        if (operator == null || !operator.equals(inst.getInitiator())) {
            throw new BizException("仅发起人可撤回流程");
        }
        LocalDateTime now = LocalDateTime.now();
        inst.setStatus("withdrawn");
        inst.setEndTime(now);
        inst.setUpdateTime(now);
        updateById(inst);

        List<FlowTask> pending = taskMapper.selectList(new LambdaQueryWrapper<FlowTask>()
                .eq(FlowTask::getInstId, id)
                .eq(FlowTask::getStatus, "pending"));
        for (FlowTask t : pending) {
            t.setStatus("skipped");
            t.setUpdateTime(now);
            taskMapper.updateById(t);
        }
        writeCallback(inst, "withdrawn");
    }

    private String nextInstNo() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        long count = count(new LambdaQueryWrapper<FlowInstance>()
                .likeRight(FlowInstance::getInstNo, "FL" + date));
        return "FL" + date + String.format("%04d", count + 1);
    }

    private void createTask(FlowInstance inst, FlowNode node) {
        LocalDateTime now = LocalDateTime.now();
        FlowTask task = new FlowTask();
        task.setInstId(inst.getId());
        task.setNodeKey(node.getNodeKey());
        task.setNodeName(node.getNodeName());
        task.setAssignee(node.getApproverValue());
        task.setStatus("pending");
        task.setCreateTime(now);
        task.setUpdateTime(now);
        taskMapper.insert(task);
    }

    private void createCc(Long instId, String ccUser) {
        if (ccUser == null || ccUser.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        FlowCc cc = new FlowCc();
        cc.setInstId(instId);
        cc.setCcUser(ccUser);
        cc.setReadFlag(0);
        cc.setCreateTime(now);
        cc.setUpdateTime(now);
        ccMapper.insert(cc);
    }

    private void writeCallback(FlowInstance inst, String event) {
        LocalDateTime now = LocalDateTime.now();
        FlowCallbackLog log = new FlowCallbackLog();
        log.setInstId(inst.getId());
        log.setBizCode(inst.getBizCode());
        log.setBizId(inst.getBizId());
        log.setEvent(event);
        log.setPayload(inst.getBizJson());
        log.setCallbackTime(now);
        log.setStatus("success");
        log.setCreateTime(now);
        log.setUpdateTime(now);
        callbackLogMapper.insert(log);
    }
}
