package com.example.flowengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.flowengine.common.BizException;
import com.example.flowengine.entity.*;
import com.example.flowengine.mapper.*;
import com.example.flowengine.service.FlowTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlowTaskServiceImpl extends ServiceImpl<FlowTaskMapper, FlowTask> implements FlowTaskService {

    private final FlowInstanceMapper instanceMapper;
    private final FlowNodeMapper nodeMapper;
    private final FlowCcMapper ccMapper;
    private final FlowCallbackLogMapper callbackLogMapper;

    public FlowTaskServiceImpl(FlowInstanceMapper instanceMapper, FlowNodeMapper nodeMapper,
                               FlowCcMapper ccMapper, FlowCallbackLogMapper callbackLogMapper) {
        this.instanceMapper = instanceMapper;
        this.nodeMapper = nodeMapper;
        this.ccMapper = ccMapper;
        this.callbackLogMapper = callbackLogMapper;
    }

    @Override
    @Transactional
    public void complete(Long id, String action, String comment) {
        FlowTask task = getById(id);
        if (task == null) {
            throw new BizException("任务不存在");
        }
        if (!"pending".equals(task.getStatus())) {
            throw new BizException("非待办任务不可处理");
        }
        FlowInstance inst = instanceMapper.selectById(task.getInstId());
        if (inst == null || !"running".equals(inst.getStatus())) {
            throw new BizException("流程已结束，不可审批");
        }
        if (!"approve".equals(action) && !"reject".equals(action)) {
            throw new BizException("不支持的操作类型：" + action);
        }
        LocalDateTime now = LocalDateTime.now();
        task.setStatus("approve".equals(action) ? "approved" : "rejected");
        task.setComment(comment);
        task.setHandleTime(now);
        task.setUpdateTime(now);
        updateById(task);

        if ("reject".equals(action)) {
            inst.setStatus("rejected");
            inst.setEndTime(now);
            inst.setUpdateTime(now);
            instanceMapper.updateById(inst);
            writeCallback(inst, "rejected");
            return;
        }
        advance(inst, task.getNodeKey(), now);
    }

    private void advance(FlowInstance inst, String currentNodeKey, LocalDateTime now) {
        List<FlowNode> nodes = nodeMapper.selectList(new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getDefId, inst.getDefId())
                .orderByAsc(FlowNode::getSortOrder));
        int currentIdx = -1;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getNodeKey() != null && nodes.get(i).getNodeKey().equals(currentNodeKey)) {
                currentIdx = i;
                break;
            }
        }
        for (int i = currentIdx + 1; i < nodes.size(); i++) {
            FlowNode node = nodes.get(i);
            if ("cc".equals(node.getNodeType())) {
                createCc(inst.getId(), node.getApproverValue());
            } else if ("approval".equals(node.getNodeType())) {
                FlowTask next = new FlowTask();
                next.setInstId(inst.getId());
                next.setNodeKey(node.getNodeKey());
                next.setNodeName(node.getNodeName());
                next.setAssignee(node.getApproverValue());
                next.setStatus("pending");
                next.setCreateTime(now);
                next.setUpdateTime(now);
                save(next);
                inst.setCurrentNode(node.getNodeKey());
                inst.setUpdateTime(now);
                instanceMapper.updateById(inst);
                return;
            }
        }
        inst.setStatus("approved");
        inst.setCurrentNode(null);
        inst.setEndTime(now);
        inst.setUpdateTime(now);
        instanceMapper.updateById(inst);
        writeCallback(inst, "approved");
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
