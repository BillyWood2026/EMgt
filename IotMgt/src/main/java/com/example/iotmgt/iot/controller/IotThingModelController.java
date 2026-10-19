package com.example.iotmgt.iot.controller;

import com.example.iotmgt.common.ResponseResult;
import com.example.iotmgt.iot.entity.IotThingModel;
import com.example.iotmgt.iot.mapper.IotThingModelMapper;
import com.example.iotmgt.iot.service.IotService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/iot/thing-model")
public class IotThingModelController {

    private final IotService iotService;
    private final IotThingModelMapper thingModelMapper;

    public IotThingModelController(IotService iotService, IotThingModelMapper thingModelMapper) {
        this.iotService = iotService;
        this.thingModelMapper = thingModelMapper;
    }

    @GetMapping("/list")
    public ResponseResult<List<IotThingModel>> list() {
        return ResponseResult.ok(iotService.listThingModels());
    }

    @GetMapping("/{id}")
    public ResponseResult<IotThingModel> getById(@PathVariable Long id) {
        return ResponseResult.ok(thingModelMapper.selectById(id));
    }

    @PostMapping
    public ResponseResult<Boolean> create(@RequestBody IotThingModel model) {
        model.setId(null);
        model.setCreateTime(LocalDateTime.now());
        if (model.getVersion() == null) model.setVersion("V1.0");
        return ResponseResult.ok(thingModelMapper.insert(model) > 0);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody IotThingModel model) {
        model.setId(id);
        model.setUpdateTime(LocalDateTime.now());
        return ResponseResult.ok(thingModelMapper.updateById(model) > 0);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(thingModelMapper.deleteById(id) > 0);
    }
}
