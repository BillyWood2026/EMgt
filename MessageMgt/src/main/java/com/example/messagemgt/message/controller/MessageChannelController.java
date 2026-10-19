package com.example.messagemgt.message.controller;

import com.example.messagemgt.common.ResponseResult;
import com.example.messagemgt.message.entity.MessageChannel;
import com.example.messagemgt.message.mapper.MessageChannelMapper;
import com.example.messagemgt.message.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/message/channel")
public class MessageChannelController {

    private final MessageService messageService;
    private final MessageChannelMapper channelMapper;

    public MessageChannelController(MessageService messageService, MessageChannelMapper channelMapper) {
        this.messageService = messageService;
        this.channelMapper = channelMapper;
    }

    @GetMapping("/list")
    public ResponseResult<List<MessageChannel>> list(@RequestParam(required = false) String channelType,
                                                     @RequestParam(required = false) String status) {
        return ResponseResult.ok(messageService.listChannels(channelType, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<MessageChannel> getById(@PathVariable Long id) {
        return ResponseResult.ok(channelMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MessageChannel channel) {
        channel.setId(null);
        channel.setCreateTime(LocalDateTime.now());
        if (channel.getStatus() == null) channel.setStatus("启用");
        return ResponseResult.ok(channelMapper.insert(channel) > 0);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MessageChannel channel) {
        channel.setId(id);
        channel.setUpdateTime(LocalDateTime.now());
        return ResponseResult.ok(channelMapper.updateById(channel) > 0);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(channelMapper.deleteById(id) > 0);
    }
}
