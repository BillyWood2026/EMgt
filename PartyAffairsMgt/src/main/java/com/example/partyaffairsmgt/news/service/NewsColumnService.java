package com.example.partyaffairsmgt.news.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.news.entity.NewsArticle;
import com.example.partyaffairsmgt.news.entity.NewsColumn;
import com.example.partyaffairsmgt.news.mapper.NewsArticleMapper;
import com.example.partyaffairsmgt.news.mapper.NewsColumnMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 栏目管理服务（需求 7.1）。
 * 栏目树、编码唯一、删除栏目时存在稿件阻断、可见范围、审批级别。
 */
@Service
public class NewsColumnService {

    @Autowired
    private NewsColumnMapper columnMapper;
    @Autowired
    private NewsArticleMapper articleMapper;
    @Autowired
    private AuditService auditService;

    /** 栏目树 */
    public List<Map<String, Object>> tree() {
        List<NewsColumn> all = columnMapper.selectList(
                new QueryWrapper<NewsColumn>().eq("deleted", 0).orderByAsc("sort_order", "id"));
        Map<Long, List<NewsColumn>> byParent = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));
        return buildChildren(0L, byParent);
    }

    private List<Map<String, Object>> buildChildren(Long parentId, Map<Long, List<NewsColumn>> byParent) {
        List<NewsColumn> children = byParent.getOrDefault(parentId, List.of());
        List<Map<String, Object>> result = new ArrayList<>();
        for (NewsColumn c : children) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", c.getId());
            node.put("name", c.getName());
            node.put("code", c.getCode());
            node.put("parentId", c.getParentId());
            node.put("sortOrder", c.getSortOrder());
            node.put("visible", c.getVisible());
            node.put("allowContribution", c.getAllowContribution());
            node.put("approveLevel", c.getApproveLevel());
            node.put("status", c.getStatus());
            node.put("children", buildChildren(c.getId(), byParent));
            result.add(node);
        }
        return result;
    }

    /** 栏目列表（平铺，供下拉） */
    public List<NewsColumn> list() {
        return columnMapper.selectList(
                new QueryWrapper<NewsColumn>().eq("deleted", 0).orderByAsc("sort_order", "id"));
    }

    /** 保存栏目（新增/编辑） */
    @Transactional
    public NewsColumn save(NewsColumn column) {
        // 编码唯一校验
        Long dup = columnMapper.selectCount(
                new QueryWrapper<NewsColumn>()
                        .eq("code", column.getCode())
                        .eq("deleted", 0)
                        .ne(column.getId() != null, "id", column.getId()));
        if (dup > 0) {
            throw PartyBizException.block("E-NEWS-01", "栏目编码已存在，不得重复");
        }
        if (column.getParentId() != null && column.getParentId().equals(column.getId())) {
            throw PartyBizException.block("E-NEWS-01", "上级栏目不能选择自身");
        }
        if (column.getId() == null) {
            column.setCreateTime(LocalDateTime.now());
            columnMapper.insert(column);
        } else {
            column.setUpdateTime(LocalDateTime.now());
            columnMapper.updateById(column);
        }
        auditService.log("新闻栏目", column.getId() == null ? "新增" : "编辑", column.getId(),
                column.getName() + "(" + column.getCode() + ")");
        return column;
    }

    /** 删除栏目：存在稿件时阻断 */
    @Transactional
    public void delete(Long id) {
        Long articleCount = articleMapper.selectCount(
                new QueryWrapper<NewsArticle>().eq("column_id", id).eq("deleted", 0));
        if (articleCount > 0) {
            throw PartyBizException.block("E-NEWS-02",
                    "栏目下存在 " + articleCount + " 篇稿件，须先迁移或下线稿件后再删除");
        }
        // 检查子栏目
        Long childCount = columnMapper.selectCount(
                new QueryWrapper<NewsColumn>().eq("parent_id", id).eq("deleted", 0));
        if (childCount > 0) {
            throw PartyBizException.block("E-NEWS-02", "栏目下存在 " + childCount + " 个子栏目，须先删除子栏目");
        }
        NewsColumn c = columnMapper.selectById(id);
        if (c != null) {
            // deleted 带 @TableLogic，updateById 会剔除该字段，必须走 deleteById 触发逻辑删除
            columnMapper.deleteById(id);
        }
        auditService.log("新闻栏目", "删除", id, CurrentHeader.operator());
    }
}
