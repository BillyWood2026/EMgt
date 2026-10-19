package com.example.sysmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmgt.entity.SysDictData;

import java.util.List;

public interface SysDictDataService extends IService<SysDictData> {

    Page<SysDictData> page(int current, int size, String dictType, String dictLabel, String status);

    List<SysDictData> list(String dictType, String dictLabel, String status);

    List<SysDictData> listByDictType(String dictType);

    boolean deleteByDictType(String dictType);
}
