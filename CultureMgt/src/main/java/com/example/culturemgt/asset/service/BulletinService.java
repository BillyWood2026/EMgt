package com.example.culturemgt.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.asset.entity.CultureBulletin;
import com.example.culturemgt.asset.mapper.CultureBulletinMapper;
import com.example.culturemgt.common.cul.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BulletinService {

    private final CultureBulletinMapper bulletinMapper;
    private final AuditService auditService;

    public BulletinService(CultureBulletinMapper bulletinMapper, AuditService auditService) {
        this.bulletinMapper = bulletinMapper;
        this.auditService = auditService;
    }

    public List<CultureBulletin> list(String status, String keyword) {
        LambdaQueryWrapper<CultureBulletin> w = new LambdaQueryWrapper<>();
        w.eq(CultureBulletin::getDeleted, 0);
        if (status != null && !status.isBlank()) w.eq(CultureBulletin::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            w.like(CultureBulletin::getTitle, keyword);
        }
        w.orderByDesc(CultureBulletin::getTopFlag).orderByDesc(CultureBulletin::getEffectTime);
        return bulletinMapper.selectList(w);
    }

    public List<CultureBulletin> publicList() {
        LambdaQueryWrapper<CultureBulletin> w = new LambdaQueryWrapper<>();
        w.eq(CultureBulletin::getDeleted, 0)
         .eq(CultureBulletin::getStatus, "已发布")
         .le(CultureBulletin::getEffectTime, LocalDateTime.now())
         .orderByDesc(CultureBulletin::getTopFlag)
         .orderByDesc(CultureBulletin::getEffectTime);
        return bulletinMapper.selectList(w);
    }

    public CultureBulletin get(Long id) {
        CultureBulletin b = bulletinMapper.selectById(id);
        AssetFlow.require(b != null && b.getDeleted() == 0, "01", "公告不存在");
        return b;
    }

    @Transactional
    public CultureBulletin save(CultureBulletin b) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        validate(b);
        if (b.getId() != null) {
            CultureBulletin old = bulletinMapper.selectById(b.getId());
            AssetFlow.require(old != null && old.getDeleted() == 0, "01", "公告不存在");
            AssetFlow.require("草稿".equals(old.getStatus()), "06", "当前状态不允许编辑");
            b.setId(old.getId());
        } else {
            b.setStatus("草稿");
            b.setCreator(CurrentHeader.operator());
            b.setViewCount(0);
        }
        b.setUpdateTime(LocalDateTime.now());
        if (b.getId() == null) {
            b.setCreateTime(LocalDateTime.now());
            bulletinMapper.insert(b);
        } else {
            bulletinMapper.updateById(b);
        }
        auditService.log(b.getId() == null ? "NEW" : "EDIT", "cul_bulletin", b.getId(),
            (b.getId() == null ? "新建公告 " : "编辑公告 ") + b.getTitle());
        return b;
    }

    @Transactional
    public void publish(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CultureBulletin b = bulletinMapper.selectById(id);
        AssetFlow.require(b != null && b.getDeleted() == 0, "01", "公告不存在");
        AssetFlow.require("草稿".equals(b.getStatus()), "06", "当前状态不允许发布");
        b.setStatus("已发布");
        b.setUpdateTime(LocalDateTime.now());
        bulletinMapper.updateById(b);
        auditService.log("PUBLISH", "cul_bulletin", id, "发布公告 " + b.getTitle());
    }

    @Transactional
    public void offline(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CultureBulletin b = bulletinMapper.selectById(id);
        AssetFlow.require(b != null && b.getDeleted() == 0, "01", "公告不存在");
        AssetFlow.require("已发布".equals(b.getStatus()), "06", "当前状态不允许下线");
        b.setStatus("已下线");
        b.setUpdateTime(LocalDateTime.now());
        bulletinMapper.updateById(b);
        auditService.log("OFFLINE", "cul_bulletin", id, "下线公告 " + b.getTitle());
    }

    @Transactional
    public void delete(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CultureBulletin b = bulletinMapper.selectById(id);
        AssetFlow.require(b != null && b.getDeleted() == 0, "01", "公告不存在");
        AssetFlow.require("草稿".equals(b.getStatus()), "06", "仅草稿可删除");
        // deleted 带 @TableLogic，updateById 会剔除该字段，必须走 deleteById 触发逻辑删除
        bulletinMapper.deleteById(id);
        auditService.log("DELETE", "cul_bulletin", id, "删除公告 " + b.getTitle());
    }

    @Transactional
    public CultureBulletin view(Long id) {
        CultureBulletin b = bulletinMapper.selectById(id);
        AssetFlow.require(b != null && b.getDeleted() == 0, "01", "公告不存在");
        b.setViewCount(b.getViewCount() + 1);
        b.setUpdateTime(LocalDateTime.now());
        bulletinMapper.updateById(b);
        return b;
    }

    private void validate(CultureBulletin b) {
        AssetFlow.require(b.getTitle() != null && b.getTitle().trim().length() >= 2 && b.getTitle().trim().length() <= 200, "11", "公告标题2-200字");
        AssetFlow.require(b.getContent() != null && b.getContent().trim().length() > 0, "12", "公告正文不能为空");
        AssetFlow.require(b.getEffectTime() != null, "13", "生效时间不能为空");
    }
}
