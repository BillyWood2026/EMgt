package com.example.messagemgt.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.messagemgt.common.BizException;
import com.example.messagemgt.message.entity.*;
import com.example.messagemgt.message.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MessageService extends ServiceImpl<MessageRecordMapper, MessageRecord> {

    private final MessageTemplateMapper templateMapper;
    private final MessageChannelMapper channelMapper;
    private final MessageStatMapper statMapper;

    public MessageService(MessageTemplateMapper templateMapper, MessageChannelMapper channelMapper,
                          MessageStatMapper statMapper) {
        this.templateMapper = templateMapper;
        this.channelMapper = channelMapper;
        this.statMapper = statMapper;
    }

    // ==================== 消息记录 ====================
    public List<MessageRecord> listRecords(String channel, String sendStatus) {
        LambdaQueryWrapper<MessageRecord> w = new LambdaQueryWrapper<>();
        if (channel != null) w.eq(MessageRecord::getChannel, channel);
        if (sendStatus != null) w.eq(MessageRecord::getSendStatus, sendStatus);
        w.orderByDesc(MessageRecord::getCreateTime);
        return baseMapper.selectList(w);
    }

    // ==================== 消息模板 ====================
    public List<MessageTemplate> listTemplates(String channel, String status) {
        LambdaQueryWrapper<MessageTemplate> w = new LambdaQueryWrapper<>();
        if (channel != null) w.eq(MessageTemplate::getChannel, channel);
        if (status != null) w.eq(MessageTemplate::getStatus, status);
        w.orderByDesc(MessageTemplate::getCreateTime);
        return templateMapper.selectList(w);
    }

    // ==================== 渠道配置 ====================
    public List<MessageChannel> listChannels(String channelType, String status) {
        LambdaQueryWrapper<MessageChannel> w = new LambdaQueryWrapper<>();
        if (channelType != null) w.eq(MessageChannel::getChannelType, channelType);
        if (status != null) w.eq(MessageChannel::getStatus, status);
        return channelMapper.selectList(w);
    }

    // ==================== 消息统计 ====================
    public List<MessageStat> listStats(String channel) {
        LambdaQueryWrapper<MessageStat> w = new LambdaQueryWrapper<>();
        if (channel != null) w.eq(MessageStat::getChannel, channel);
        w.orderByDesc(MessageStat::getStatDate);
        return statMapper.selectList(w);
    }

    // ==================== 统一消息发送 ====================
    @Transactional
    public MessageRecord send(String templateCode, String receiver, Map<String, Object> bizParam) {
        MessageTemplate tpl = templateMapper.selectOne(
                new LambdaQueryWrapper<MessageTemplate>().eq(MessageTemplate::getTemplateCode, templateCode));
        if (tpl == null) throw new BizException(50001, "模板不存在：" + templateCode);
        if ("停用".equals(tpl.getStatus())) throw new BizException(50002, "模板未启用：" + templateCode);

        MessageRecord record = new MessageRecord();
        record.setTemplateCode(templateCode);
        record.setChannel(tpl.getChannel());
        record.setReceiver(receiver);
        record.setTitle(render(tpl.getTitleTemplate(), bizParam));
        record.setContent(render(tpl.getContentTemplate(), bizParam));
        record.setBizParam(bizParam == null ? null : bizParam.toString());
        // 模拟投递：直接置为已发送
        record.setSendStatus("已发送");
        record.setSendTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        baseMapper.insert(record);
        return record;
    }

    // 模板变量渲染：${key} 占位符替换
    private String render(String template, Map<String, Object> params) {
        if (template == null) return null;
        String result = template;
        if (params != null) {
            for (Map.Entry<String, Object> e : params.entrySet()) {
                result = result.replace("${" + e.getKey() + "}", String.valueOf(e.getValue()));
            }
        }
        return result;
    }
}
