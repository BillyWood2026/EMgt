package com.example.messagemgt.message.controller;

import com.example.messagemgt.common.ResponseResult;
import com.example.messagemgt.message.entity.MessageRecord;
import com.example.messagemgt.message.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageSendController {

    private final MessageService messageService;

    public MessageSendController(MessageService messageService) {
        this.messageService = messageService;
    }

    // 统一消息发送：模板渲染 + 模拟投递
    @PostMapping("/send")
    @SuppressWarnings("unchecked")
    public ResponseResult<MessageRecord> send(@RequestBody Map<String, Object> body) {
        String templateCode = (String) body.get("templateCode");
        String receiver = (String) body.get("receiver");
        Object bizParam = body.get("bizParam");
        Map<String, Object> params = bizParam instanceof Map ? (Map<String, Object>) bizParam : null;
        return ResponseResult.ok(messageService.send(templateCode, receiver, params));
    }
}
