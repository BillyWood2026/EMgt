package com.example.sysmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmgt.entity.SysFile;

import java.util.List;

public interface SysFileService extends IService<SysFile> {

    Page<SysFile> page(int current, int size, String fileName, String fileType, String storageType, String bizType);

    List<SysFile> list(String fileName, String fileType, String storageType, String bizType);

    List<SysFile> listByBiz(String bizType, String bizId);

    boolean deleteByBiz(String bizType, String bizId);
}
