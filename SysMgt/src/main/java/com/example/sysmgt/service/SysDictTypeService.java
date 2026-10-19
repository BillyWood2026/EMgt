package com.example.sysmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmgt.entity.SysDictType;

import java.util.List;

public interface SysDictTypeService extends IService<SysDictType> {

    Page<SysDictType> page(int current, int size, String dictName, String dictType, String status);

    List<SysDictType> list(String dictName, String dictType, String status);

    SysDictType getByDictType(String dictType);

    boolean checkDictTypeUnique(Long id, String dictType);
}
