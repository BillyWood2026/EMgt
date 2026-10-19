package com.example.messagemgt.message.controller;

import com.example.messagemgt.common.ResponseResult;
import com.example.messagemgt.message.entity.MessageTemplate;
import com.example.messagemgt.message.mapper.MessageTemplateMapper;
import com.example.messagemgt.message.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/message/template")
public class MessageTemplateController {

    private final MessageService messageService;
    private final MessageTemplateMapper templateMapper;

    public MessageTemplateController(MessageService messageService, MessageTemplateMapper templateMapper) {
        this.messageService = messageService;
        this.templateMapper = templateMapper;
    }

    @GetMapping("/list")
    public ResponseResult<List<MessageTemplate>> list(@RequestParam(required = false) String channel,
                                                      @RequestParam(required = false) String status) {
        return ResponseResult.ok(messageService.listTemplates(channel, status));
    }

    @GetMapping("/{id}")
    public ResponseResult<MessageTemplate> getById(@PathVariable Long id) {
        return ResponseResult.ok(templateMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MessageTemplate template) {
        template.setId(null);
        template.setCreateTime(LocalDateTime.now());
        if (template.getStatus() == null) template.setStatus("启用");
        return ResponseResult.ok(templateMapper.insert(template) > 0);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MessageTemplate template) {
        template.setId(id);
        template.setUpdateTime(LocalDateTime.now());
        return ResponseResult.ok(templateMapper.updateById(template) > 0);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(templateMapper.deleteById(id) > 0);
    }
}
