package com.example.partyaffairsmgt.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.system.entity.SysMsgRecord;
import com.example.partyaffairsmgt.system.entity.SysMsgTemplate;
import com.example.partyaffairsmgt.system.mapper.SysMsgRecordMapper;
import com.example.partyaffairsmgt.system.mapper.SysMsgTemplateMapper;
import com.example.partyaffairsmgt.system.service.SysAuditWriter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知（需求 12.4：站内信/短信/邮件/移动推送，夜间静默，失败重试上限3次）。
 * E-SY-11 消息重试超3次(预警)；E-SY-16 必填缺失。
 */
@RestController
@RequestMapping("/api/party/system/message")
@CrossOrigin(origins = "*")
public class SysMessageController {

    private static final int MAX_RETRY = 3;
    private static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(7, 0);

    @Autowired
    private SysMsgTemplateMapper templateMapper;
    @Autowired
    private SysMsgRecordMapper recordMapper;
    @Autowired
    private SysAuditWriter auditWriter;

    // ---------------- 模板 ----------------

    @GetMapping("/template/list")
    public ResponseResult<List<SysMsgTemplate>> templateList() {
        PartyRole.requireAny(PartyRole.READ);
        QueryWrapper<SysMsgTemplate> qw = new QueryWrapper<>();
        qw.orderByAsc("tpl_code");
        return ResponseResult.success(templateMapper.selectList(qw));
    }

    @PostMapping("/template")
    public ResponseResult<SysMsgTemplate> createTemplate(@RequestBody SysMsgTemplate tpl) {
        PartyRole.requireAny(PartyRole.R02);
        if (tpl.getTplCode() == null || tpl.getTplCode().isBlank()
                || tpl.getTplName() == null || tpl.getTplName().isBlank()) {
            throw PartyBizException.block("E-SY-16", "必填项缺失：模板编码/模板名称");
        }
        QueryWrapper<SysMsgTemplate> qw = new QueryWrapper<>();
        qw.eq("tpl_code", tpl.getTplCode());
        if (templateMapper.selectCount(qw) > 0) {
            throw PartyBizException.block("E-SY-16", "模板编码已存在：" + tpl.getTplCode());
        }
        LocalDateTime now = LocalDateTime.now();
        tpl.setId(null);
        if (tpl.getMsgType() == null) tpl.setMsgType("通知");
        if (tpl.getChannels() == null) tpl.setChannels("站内信");
        if (tpl.getStatus() == null) tpl.setStatus("启用");
        tpl.setCreateTime(now);
        tpl.setUpdateTime(now);
        templateMapper.insert(tpl);
        auditWriter.write("操作", "新增消息模板", "sys_msg_template", tpl.getId(), null, tpl.getTplCode(), null);
        return ResponseResult.success(tpl);
    }

    @PutMapping("/template/{id}")
    public ResponseResult<SysMsgTemplate> updateTemplate(@PathVariable Long id, @RequestBody SysMsgTemplate tpl) {
        PartyRole.requireAny(PartyRole.R02);
        tpl.setId(id);
        tpl.setTplCode(null);
        tpl.setCreateTime(null);
        tpl.setUpdateTime(LocalDateTime.now());
        templateMapper.updateById(tpl);
        auditWriter.write("操作", "修改消息模板", "sys_msg_template", id, null, tpl.getTplName(), null);
        return ResponseResult.success(templateMapper.selectById(id));
    }

    @DeleteMapping("/template/{id}")
    public ResponseResult<Boolean> deleteTemplate(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        boolean ok = templateMapper.deleteById(id) > 0;
        auditWriter.write("操作", "删除消息模板", "sys_msg_template", id, null, null, null);
        return ResponseResult.success(ok);
    }

    // ---------------- 发送记录 ----------------

    @GetMapping("/record/list")
    public ResponseResult<Page<SysMsgRecord>> recordList(@RequestParam(required = false) String status,
                                                         @RequestParam(required = false) String msgType,
                                                         @RequestParam(required = false) String receiverLike,
                                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        PartyRole.requireAny(PartyRole.READ);
        QueryWrapper<SysMsgRecord> qw = new QueryWrapper<>();
        if (status != null && !status.isBlank()) qw.eq("status", status);
        if (msgType != null && !msgType.isBlank()) qw.eq("msg_type", msgType);
        if (receiverLike != null && !receiverLike.isBlank()) qw.like("receiver", receiverLike);
        qw.orderByDesc("id");
        return ResponseResult.success(recordMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    /** 按模板发送：逐渠道拆分生成记录；夜间静默仅对非预警且开启静默的模板生效。 */
    @PostMapping("/send")
    public ResponseResult<Map<String, Object>> send(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02);
        String tplCode = (String) body.get("tplCode");
        String receiver = (String) body.get("receiver");
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        boolean failDemo = Boolean.parseBoolean(String.valueOf(body.getOrDefault("fail", "false")));
        if (tplCode == null || receiver == null || content == null) {
            throw PartyBizException.block("E-SY-16", "必填项缺失：模板编码/接收人/内容");
        }
        QueryWrapper<SysMsgTemplate> qw = new QueryWrapper<>();
        qw.eq("tpl_code", tplCode);
        SysMsgTemplate tpl = templateMapper.selectOne(qw);
        if (tpl == null) throw PartyBizException.block("E-SY-16", "消息模板不存在：" + tplCode);

        boolean nightSilent = !"预警".equals(tpl.getMsgType())
                && tpl.getNightSilent() != null && tpl.getNightSilent() == 1
                && isNight(LocalTime.now());
        LocalDateTime now = LocalDateTime.now();
        List<Long> ids = new ArrayList<>();
        for (String ch : tpl.getChannels().split(",")) {
            SysMsgRecord r = new SysMsgRecord();
            r.setTplCode(tplCode);
            r.setTitle(title);
            r.setContent(content);
            r.setReceiver(receiver);
            r.setChannel(ch.trim());
            r.setMsgType(tpl.getMsgType());
            r.setRetryCount(0);
            r.setCreateTime(now);
            if (nightSilent) {
                r.setStatus("待发送");
                r.setScheduledTime(nextMorning(now));
            } else if (failDemo) {
                r.setStatus("发送失败");
                r.setFailReason("模拟渠道发送失败(演示)");
            } else {
                r.setStatus("已发送");
                r.setSendTime(now);
            }
            recordMapper.insert(r);
            ids.add(r.getId());
        }
        auditWriter.write("操作", "发送消息", "sys_msg_record", null, null,
                "tpl=" + tplCode + ",receiver=" + receiver + ",channels=" + tpl.getChannels(), null);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordIds", ids);
        result.put("nightSilent", nightSilent);
        result.put("channels", tpl.getChannels().split(",").length);
        return ResponseResult.success(result);
    }

    /** 失败重发：重试次数+1，达上限3次保持失败并预警(E-SY-11)。 */
    @PostMapping("/{id}/resend")
    public ResponseResult<Map<String, Object>> resend(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        SysMsgRecord r = recordMapper.selectById(id);
        if (r == null) throw PartyBizException.block("E-SY-16", "消息记录不存在：" + id);
        int retry = (r.getRetryCount() == null ? 0 : r.getRetryCount()) + 1;
        SysMsgRecord u = new SysMsgRecord();
        u.setId(id);
        u.setRetryCount(retry);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("retryCount", retry);
        if (retry >= MAX_RETRY) {
            u.setStatus("发送失败");
            u.setFailReason("重试" + retry + "次仍失败，已达上限" + MAX_RETRY);
            recordMapper.updateById(u);
            result.put("status", "发送失败");
            result.put("warn", "[E-SY-11][预警] 消息重试超" + MAX_RETRY + "次，请检查渠道配置：" + r.getTitle());
        } else {
            u.setStatus("已发送");
            u.setSendTime(LocalDateTime.now());
            u.setFailReason(null);
            recordMapper.updateById(u);
            result.put("status", "已发送");
        }
        auditWriter.write("操作", "消息重发", "sys_msg_record", id, null,
                "retry=" + retry + ",status=" + result.get("status"), null);
        return ResponseResult.success(result);
    }

    private static boolean isNight(LocalTime t) {
        return !t.isBefore(NIGHT_START) || t.isBefore(NIGHT_END);
    }

    private static LocalDateTime nextMorning(LocalDateTime now) {
        LocalDateTime base = now.toLocalDate().atTime(NIGHT_END);
        return now.toLocalTime().isBefore(NIGHT_END) ? base : base.plusDays(1);
    }
}
