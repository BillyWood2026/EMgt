package com.example.iam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.iam.entity.system.SysDict;
import com.example.iam.entity.system.SysDictItem;
import com.example.iam.mapper.SysDictItemMapper;
import com.example.iam.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/sys/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictMapper dictMapper;
    private final SysDictItemMapper dictItemMapper;

    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictCode,
            @RequestParam(required = false) String status) {

        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        if (dictName != null && !dictName.isEmpty()) {
            wrapper.like(SysDict::getDictName, dictName);
        }
        if (dictCode != null && !dictCode.isEmpty()) {
            wrapper.like(SysDict::getDictCode, dictCode);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SysDict::getStatus, status.toUpperCase());
        }
        wrapper.orderByAsc(SysDict::getSortOrder);

        Page<SysDict> page = new Page<>(pageNum, pageSize);
        Page<SysDict> result = dictMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = result.getRecords().stream().map(dict -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", dict.getId());
            map.put("dictCode", dict.getDictCode());
            map.put("dictName", dict.getDictName());
            map.put("description", dict.getDescription());
            map.put("sortOrder", dict.getSortOrder());
            map.put("status", dict.getStatus());
            map.put("createTime", dict.getCreatedTime());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return buildSuccessResponse(data);
    }

    @GetMapping("/all")
    public Map<String, Object> getAll() {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getStatus, "ACTIVE");
        wrapper.orderByAsc(SysDict::getSortOrder);
        List<SysDict> dicts = dictMapper.selectList(wrapper);
        List<Map<String, Object>> list = dicts.stream().map(dict -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", dict.getId());
            map.put("dictCode", dict.getDictCode());
            map.put("dictName", dict.getDictName());
            return map;
        }).collect(Collectors.toList());
        return buildSuccessResponse(list);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        SysDict dict = dictMapper.selectById(id);
        if (dict == null) {
            return buildErrorResponse("字典不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", dict.getId());
        data.put("dictCode", dict.getDictCode());
        data.put("dictName", dict.getDictName());
        data.put("description", dict.getDescription());
        data.put("sortOrder", dict.getSortOrder());
        data.put("status", dict.getStatus());
        return buildSuccessResponse(data);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> request) {
        String dictCode = (String) request.get("dictCode");
        String dictName = (String) request.get("dictName");

        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictCode, dictCode);
        if (dictMapper.selectCount(wrapper) > 0) {
            return buildErrorResponse("字典编码已存在");
        }

        SysDict dict = new SysDict();
        dict.setDictCode(dictCode);
        dict.setDictName(dictName);
        dict.setDescription((String) request.get("description"));
        dict.setSortOrder(request.get("sortOrder") != null ? (Integer) request.get("sortOrder") : 0);
        dict.setStatus("ACTIVE");
        dict.setCreatedBy("SYSTEM");
        dict.setCreatedTime(LocalDateTime.now());

        dictMapper.insert(dict);
        log.info("Dict created: {}", dictCode);
        return buildSuccessResponse("创建成功", null);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        SysDict dict = dictMapper.selectById(id);
        if (dict == null) {
            return buildErrorResponse("字典不存在");
        }

        if (request.containsKey("dictName")) {
            dict.setDictName((String) request.get("dictName"));
        }
        if (request.containsKey("description")) {
            dict.setDescription((String) request.get("description"));
        }
        if (request.containsKey("sortOrder")) {
            dict.setSortOrder((Integer) request.get("sortOrder"));
        }
        if (request.containsKey("status")) {
            dict.setStatus(((String) request.get("status")).toUpperCase());
        }
        dict.setUpdatedBy("SYSTEM");
        dict.setUpdatedTime(LocalDateTime.now());

        dictMapper.updateById(dict);
        return buildSuccessResponse("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        SysDict dict = dictMapper.selectById(id);
        if (dict == null) {
            return buildErrorResponse("字典不存在");
        }
        dictMapper.deleteById(id);
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItem::getDictId, id);
        dictItemMapper.delete(wrapper);
        return buildSuccessResponse("删除成功", null);
    }

    @GetMapping("/{id}/items")
    public Map<String, Object> getDictItems(@PathVariable Long id) {
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItem::getDictId, id);
        wrapper.eq(SysDictItem::getStatus, "ACTIVE");
        wrapper.orderByAsc(SysDictItem::getSortOrder);
        List<SysDictItem> items = dictItemMapper.selectList(wrapper);
        List<Map<String, Object>> list = items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("itemLabel", item.getItemLabel());
            map.put("itemValue", item.getItemValue());
            map.put("sortOrder", item.getSortOrder());
            map.put("status", item.getStatus());
            map.put("remark", item.getRemark());
            return map;
        }).collect(Collectors.toList());
        return buildSuccessResponse(list);
    }

    @PostMapping("/{id}/items")
    public Map<String, Object> addDictItem(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        SysDict dict = dictMapper.selectById(id);
        if (dict == null) {
            return buildErrorResponse("字典不存在");
        }

        SysDictItem item = new SysDictItem();
        item.setDictId(id);
        item.setDictCode(dict.getDictCode());
        item.setItemLabel((String) request.get("itemLabel"));
        item.setItemValue((String) request.get("itemValue"));
        item.setSortOrder(request.get("sortOrder") != null ? (Integer) request.get("sortOrder") : 0);
        item.setStatus("ACTIVE");
        item.setRemark((String) request.get("remark"));
        item.setCreatedBy("SYSTEM");
        item.setCreatedTime(LocalDateTime.now());

        dictItemMapper.insert(item);
        return buildSuccessResponse("添加成功", null);
    }

    @PutMapping("/items/{itemId}")
    public Map<String, Object> updateDictItem(@PathVariable Long itemId, @RequestBody Map<String, Object> request) {
        SysDictItem item = dictItemMapper.selectById(itemId);
        if (item == null) {
            return buildErrorResponse("字典项不存在");
        }

        if (request.containsKey("itemLabel")) {
            item.setItemLabel((String) request.get("itemLabel"));
        }
        if (request.containsKey("itemValue")) {
            item.setItemValue((String) request.get("itemValue"));
        }
        if (request.containsKey("sortOrder")) {
            item.setSortOrder((Integer) request.get("sortOrder"));
        }
        if (request.containsKey("status")) {
            item.setStatus(((String) request.get("status")).toUpperCase());
        }
        if (request.containsKey("remark")) {
            item.setRemark((String) request.get("remark"));
        }
        item.setUpdatedBy("SYSTEM");
        item.setUpdatedTime(LocalDateTime.now());

        dictItemMapper.updateById(item);
        return buildSuccessResponse("更新成功", null);
    }

    @DeleteMapping("/items/{itemId}")
    public Map<String, Object> deleteDictItem(@PathVariable Long itemId) {
        dictItemMapper.deleteById(itemId);
        return buildSuccessResponse("删除成功", null);
    }

    @GetMapping("/code/{dictCode}")
    public Map<String, Object> getDictByCode(@PathVariable String dictCode) {
        LambdaQueryWrapper<SysDict> dictWrapper = new LambdaQueryWrapper<>();
        dictWrapper.eq(SysDict::getDictCode, dictCode);
        SysDict dict = dictMapper.selectOne(dictWrapper);
        if (dict == null) {
            return buildSuccessResponse(Collections.emptyList());
        }

        LambdaQueryWrapper<SysDictItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(SysDictItem::getDictId, dict.getId());
        itemWrapper.eq(SysDictItem::getStatus, "ACTIVE");
        itemWrapper.orderByAsc(SysDictItem::getSortOrder);
        List<SysDictItem> items = dictItemMapper.selectList(itemWrapper);

        List<Map<String, Object>> list = items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("label", item.getItemLabel());
            map.put("value", item.getItemValue());
            return map;
        }).collect(Collectors.toList());

        return buildSuccessResponse(list);
    }

    private Map<String, Object> buildSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", message);
        return response;
    }
}
