package com.example.iam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.iam.entity.system.SysOperLog;
import com.example.iam.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/sys/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysOperLogMapper operLogMapper;

    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String operName,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(SysOperLog::getTitle, title);
        }
        if (operName != null && !operName.isEmpty()) {
            wrapper.like(SysOperLog::getOperName, operName);
        }
        if (businessType != null && !businessType.isEmpty()) {
            wrapper.eq(SysOperLog::getBusinessType, businessType);
        }
        if (status != null) {
            wrapper.eq(SysOperLog::getStatus, status);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(SysOperLog::getOperTime, LocalDateTime.parse(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(SysOperLog::getOperTime, LocalDateTime.parse(endTime));
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);

        Page<SysOperLog> page = new Page<>(pageNum, pageSize);
        Page<SysOperLog> result = operLogMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = result.getRecords().stream().map(l -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", l.getId());
            map.put("title", l.getTitle());
            map.put("businessType", l.getBusinessType());
            map.put("method", l.getMethod());
            map.put("requestMethod", l.getRequestMethod());
            map.put("operName", l.getOperName());
            map.put("operUrl", l.getOperUrl());
            map.put("operIp", l.getOperIp());
            map.put("operLocation", l.getOperLocation());
            map.put("status", l.getStatus());
            map.put("errorMsg", l.getErrorMsg());
            map.put("operTime", l.getOperTime());
            map.put("costTime", l.getCostTime());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return buildSuccessResponse(data);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        SysOperLog log = operLogMapper.selectById(id);
        if (log == null) {
            return buildErrorResponse("日志不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", log.getId());
        data.put("title", log.getTitle());
        data.put("businessType", log.getBusinessType());
        data.put("method", log.getMethod());
        data.put("requestMethod", log.getRequestMethod());
        data.put("operatorType", log.getOperatorType());
        data.put("operName", log.getOperName());
        data.put("deptName", log.getDeptName());
        data.put("operUrl", log.getOperUrl());
        data.put("operIp", log.getOperIp());
        data.put("operLocation", log.getOperLocation());
        data.put("operParam", log.getOperParam());
        data.put("jsonResult", log.getJsonResult());
        data.put("status", log.getStatus());
        data.put("errorMsg", log.getErrorMsg());
        data.put("operTime", log.getOperTime());
        data.put("costTime", log.getCostTime());
        return buildSuccessResponse(data);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        operLogMapper.deleteById(id);
        return buildSuccessResponse("删除成功", null);
    }

    @DeleteMapping("/clean")
    public Map<String, Object> clean() {
        operLogMapper.delete(null);
        return buildSuccessResponse("清空成功", null);
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
