package com.example.sysmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sysmgt.entity.SysNotice;

import java.util.List;

public interface SysNoticeService extends IService<SysNotice> {

    Page<SysNotice> page(int current, int size, String noticeTitle, String noticeType, String status);

    List<SysNotice> list(String noticeTitle, String noticeType, String status);

    boolean publish(Long id);

    boolean offline(Long id);

    List<SysNotice> listPublished(String noticeType);
}
