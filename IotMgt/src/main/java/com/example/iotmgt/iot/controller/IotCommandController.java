package com.example.iotmgt.iot.controller;

import com.example.iotmgt.common.ResponseResult;
import com.example.iotmgt.iot.entity.IotCommand;
import com.example.iotmgt.iot.mapper.IotCommandMapper;
import com.example.iotmgt.iot.service.IotService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/iot/command")
public class IotCommandController {

    private final IotService iotService;
    private final IotCommandMapper commandMapper;

    public IotCommandController(IotService iotService, IotCommandMapper commandMapper) {
        this.iotService = iotService;
        this.commandMapper = commandMapper;
    }

    @GetMapping("/list")
    public ResponseResult<List<IotCommand>> list(@RequestParam(required = false) String deviceCode,
                                                 @RequestParam(required = false) String execStatus) {
        return ResponseResult.ok(iotService.listCommands(deviceCode, execStatus));
    }

    @GetMapping("/{id}")
    public ResponseResult<IotCommand> getById(@PathVariable Long id) {
        return ResponseResult.ok(commandMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody IotCommand command) {
        command.setId(null);
        if (command.getIssueTime() == null) command.setIssueTime(LocalDateTime.now());
        if (command.getExecStatus() == null) command.setExecStatus("已下发");
        command.setCreateTime(LocalDateTime.now());
        return ResponseResult.ok(commandMapper.insert(command) > 0);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody IotCommand command) {
        command.setId(id);
        command.setUpdateTime(LocalDateTime.now());
        return ResponseResult.ok(commandMapper.updateById(command) > 0);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(commandMapper.deleteById(id) > 0);
    }

    // 指令执行回执：标记已执行
    @PostMapping("/{id}/execute")
    public ResponseResult<IotCommand> execute(@PathVariable Long id) {
        return ResponseResult.ok(iotService.executeCommand(id));
    }
}
