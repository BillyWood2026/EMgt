package com.example.holiday.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;

import java.util.List;

/**
 * 分页响应结果。
 * 通过 {@link JsonUnwrapped} 将 {@link ResponseResult} 信封字段
 * (code/message/msg/data/timestamp) 平铺到 JSON 顶层，
 * 同时在顶层暴露前端页面直接读取的 records / total / current / size / pages。
 */
@Data
public class PageResult<T> {

    @JsonUnwrapped
    private ResponseResult<List<T>> result;

    private List<T> records;
    private long total;
    private long current;
    private long size;
    private long pages;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> wrapper = new PageResult<>();
        wrapper.result = ResponseResult.success(page.getRecords());
        wrapper.records = page.getRecords();
        wrapper.total = page.getTotal();
        wrapper.current = page.getCurrent();
        wrapper.size = page.getSize();
        wrapper.pages = page.getPages();
        return wrapper;
    }
}
