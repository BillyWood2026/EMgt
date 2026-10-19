package com.example.messagemgt.message.controller;

import com.example.messagemgt.common.ResponseResult;
import com.example.messagemgt.message.entity.MessageRecord;
import com.example.messagemgt.message.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/message/record")
public class MessageRecordController {

    private final MessageService messageService;

    public MessageRecordController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/list")
    public ResponseResult<List<MessageRecord>> list(@RequestParam(required = false) String channel,
                                                    @RequestParam(required = false) String sendStatus) {
        return ResponseResult.ok(messageService.listRecords(channel, sendStatus));
    }

    @GetMapping("/{id}")
    public ResponseResult<MessageRecord> getById(@PathVariable Long id) {
        return ResponseResult.ok(messageService.getById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MessageRecord record) {
        record.setId(null);
        record.setCreateTime(LocalDateTime.now());
        if (record.getSendStatus() == null) record.setSendStatus("待发送");
        return ResponseResult.ok(messageService.save(record));
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MessageRecord record) {
        record.setId(id);
        record.setUpdateTime(LocalDateTime.now());
        return ResponseResult.ok(messageService.updateById(record));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(messageService.removeById(id));
    }
}
