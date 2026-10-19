package com.example.sysmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sysmgt.entity.SysNotice;
import com.example.sysmgt.mapper.SysNoticeMapper;
import com.example.sysmgt.service.SysNoticeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {

    @Override
    public Page<SysNotice> page(int current, int size, String noticeTitle, String noticeType, String status) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(noticeTitle)) {
            wrapper.like(SysNotice::getNoticeTitle, noticeTitle);
        }
        if (StringUtils.hasText(noticeType)) {
            wrapper.eq(SysNotice::getNoticeType, noticeType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysNotice::getStatus, status);
        }
        wrapper.orderByDesc(SysNotice::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<SysNotice> list(String noticeTitle, String noticeType, String status) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(noticeTitle)) {
            wrapper.like(SysNotice::getNoticeTitle, noticeTitle);
        }
        if (StringUtils.hasText(noticeType)) {
            wrapper.eq(SysNotice::getNoticeType, noticeType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysNotice::getStatus, status);
        }
        wrapper.orderByDesc(SysNotice::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean publish(Long id) {
        SysNotice notice = getById(id);
        if (notice == null) {
            return false;
        }
        notice.setStatus("PUBLISHED");
        notice.setPublishTime(LocalDateTime.now());
        return updateById(notice);
    }

    @Override
    public boolean offline(Long id) {
        SysNotice notice = getById(id);
        if (notice == null) {
            return false;
        }
        notice.setStatus("OFFLINE");
        return updateById(notice);
    }

    @Override
    public List<SysNotice> listPublished(String noticeType) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getStatus, "PUBLISHED");
        if (StringUtils.hasText(noticeType)) {
            wrapper.eq(SysNotice::getNoticeType, noticeType);
        }
        wrapper.orderByDesc(SysNotice::getPublishTime);
        return list(wrapper);
    }
}
