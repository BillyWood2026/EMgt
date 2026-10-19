package com.example.culturemgt.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.asset.entity.CultureJournal;
import com.example.culturemgt.asset.mapper.CultureJournalMapper;
import com.example.culturemgt.common.cul.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JournalService {

    private final CultureJournalMapper journalMapper;
    private final AuditService auditService;

    public JournalService(CultureJournalMapper journalMapper, AuditService auditService) {
        this.journalMapper = journalMapper;
        this.auditService = auditService;
    }

    public List<CultureJournal> list(String status, String keyword) {
        LambdaQueryWrapper<CultureJournal> w = new LambdaQueryWrapper<>();
        w.eq(CultureJournal::getDeleted, 0);
        if (status != null && !status.isBlank()) w.eq(CultureJournal::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            w.and(x -> x.like(CultureJournal::getJournalName, keyword)
                        .or().like(CultureJournal::getIssueNo, keyword));
        }
        w.orderByDesc(CultureJournal::getPublishTime);
        return journalMapper.selectList(w);
    }

    public List<CultureJournal> publicList() {
        LambdaQueryWrapper<CultureJournal> w = new LambdaQueryWrapper<>();
        w.eq(CultureJournal::getDeleted, 0)
         .eq(CultureJournal::getStatus, "已发布")
         .eq(CultureJournal::getIsPublic, 1)
         .orderByDesc(CultureJournal::getPublishTime);
        return journalMapper.selectList(w);
    }

    public CultureJournal get(Long id) {
        CultureJournal j = journalMapper.selectById(id);
        AssetFlow.require(j != null && j.getDeleted() == 0, "01", "内刊不存在");
        return j;
    }

    @Transactional
    public CultureJournal save(CultureJournal j) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        validate(j);
        if (j.getId() != null) {
            CultureJournal old = journalMapper.selectById(j.getId());
            AssetFlow.require(old != null && old.getDeleted() == 0, "01", "内刊不存在");
            AssetFlow.require("草稿".equals(old.getStatus()), "06", "当前状态不允许编辑");
            j.setId(old.getId());
        } else {
            j.setStatus("草稿");
            j.setCreator(CurrentHeader.operator());
            j.setViewCount(0);
        }
        j.setUpdateTime(LocalDateTime.now());
        if (j.getId() == null) {
            j.setCreateTime(LocalDateTime.now());
            journalMapper.insert(j);
        } else {
            journalMapper.updateById(j);
        }
        auditService.log(j.getId() == null ? "NEW" : "EDIT", "cul_journal", j.getId(),
            (j.getId() == null ? "新建内刊 " : "编辑内刊 ") + j.getIssueNo());
        return j;
    }

    @Transactional
    public void publish(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CultureJournal j = journalMapper.selectById(id);
        AssetFlow.require(j != null && j.getDeleted() == 0, "01", "内刊不存在");
        AssetFlow.require("草稿".equals(j.getStatus()), "06", "当前状态不允许发布");
        j.setStatus("已发布");
        j.setPublishTime(LocalDateTime.now());
        j.setUpdateTime(LocalDateTime.now());
        journalMapper.updateById(j);
        auditService.log("PUBLISH", "cul_journal", id, "发布内刊 " + j.getIssueNo());
    }

    @Transactional
    public void offline(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CultureJournal j = journalMapper.selectById(id);
        AssetFlow.require(j != null && j.getDeleted() == 0, "01", "内刊不存在");
        AssetFlow.require("已发布".equals(j.getStatus()), "06", "当前状态不允许下线");
        j.setStatus("已下线");
        j.setUpdateTime(LocalDateTime.now());
        journalMapper.updateById(j);
        auditService.log("OFFLINE", "cul_journal", id, "下线内刊 " + j.getIssueNo());
    }

    @Transactional
    public void delete(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CultureJournal j = journalMapper.selectById(id);
        AssetFlow.require(j != null && j.getDeleted() == 0, "01", "内刊不存在");
        AssetFlow.require("草稿".equals(j.getStatus()), "06", "仅草稿可删除");
        // deleted 带 @TableLogic，updateById 会剔除该字段，必须走 deleteById 触发逻辑删除
        journalMapper.deleteById(id);
        auditService.log("DELETE", "cul_journal", id, "删除内刊 " + j.getIssueNo());
    }

    @Transactional
    public CultureJournal view(Long id) {
        CultureJournal j = journalMapper.selectById(id);
        AssetFlow.require(j != null && j.getDeleted() == 0, "01", "内刊不存在");
        j.setViewCount(j.getViewCount() + 1);
        j.setUpdateTime(LocalDateTime.now());
        journalMapper.updateById(j);
        return j;
    }

    private void validate(CultureJournal j) {
        AssetFlow.require(j.getIssueNo() != null && j.getIssueNo().trim().length() >= 2 && j.getIssueNo().trim().length() <= 20, "07", "期号2-20字");
        AssetFlow.require(j.getJournalName() != null && j.getJournalName().trim().length() >= 2 && j.getJournalName().trim().length() <= 100, "08", "刊名2-100字");
        AssetFlow.require(j.getContent() != null && j.getContent().trim().length() > 0, "09", "正文不能为空");
        AssetFlow.require(j.getFileUrl() != null && j.getFileUrl().trim().length() > 0, "10", "附件PDF不能为空");
    }
}
