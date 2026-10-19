package com.example.messagemgt.message.controller;

import com.example.messagemgt.common.ResponseResult;
import com.example.messagemgt.message.entity.MessageStat;
import com.example.messagemgt.message.mapper.MessageStatMapper;
import com.example.messagemgt.message.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/message/stat")
public class MessageStatController {

    private final MessageService messageService;
    private final MessageStatMapper statMapper;

    public MessageStatController(MessageService messageService, MessageStatMapper statMapper) {
        this.messageService = messageService;
        this.statMapper = statMapper;
    }

    @GetMapping("/list")
    public ResponseResult<List<MessageStat>> list(@RequestParam(required = false) String channel) {
        return ResponseResult.ok(messageService.listStats(channel));
    }

    @GetMapping("/{id}")
    public ResponseResult<MessageStat> getById(@PathVariable Long id) {
        return ResponseResult.ok(statMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody MessageStat stat) {
        stat.setId(null);
        calcSuccessRate(stat);
        stat.setCreateTime(LocalDateTime.now());
        return ResponseResult.ok(statMapper.insert(stat) > 0);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody MessageStat stat) {
        stat.setId(id);
        calcSuccessRate(stat);
        stat.setUpdateTime(LocalDateTime.now());
        return ResponseResult.ok(statMapper.updateById(stat) > 0);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(statMapper.deleteById(id) > 0);
    }

    // 成功率 = 成功量/发送量×100%
    private void calcSuccessRate(MessageStat stat) {
        if (stat.getSendCount() != null && stat.getSendCount() > 0 && stat.getSuccessCount() != null) {
            stat.setSuccessRate(new BigDecimal(stat.getSuccessCount())
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(stat.getSendCount()), 2, RoundingMode.HALF_UP));
        }
    }
}
