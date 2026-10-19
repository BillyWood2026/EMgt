package com.example.culturemgt.news.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.news.entity.News;
import com.example.culturemgt.news.entity.NewsTag;
import com.example.culturemgt.news.mapper.NewsMapper;
import com.example.culturemgt.news.mapper.NewsTagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 子模块9 新闻标签字典。维护为集团文化管理员专属（C03/C99）；
 * 停用标签不会删除历史稿件关联，仅新建稿件不可选择。
 */
@Service
public class NewsTagService {

    private final NewsTagMapper tagMapper;
    private final NewsMapper newsMapper;
    private final AuditService auditService;

    public NewsTagService(NewsTagMapper tagMapper, NewsMapper newsMapper, AuditService auditService) {
        this.tagMapper = tagMapper;
        this.newsMapper = newsMapper;
        this.auditService = auditService;
    }

    public List<NewsTag> list() {
        List<NewsTag> tags = tagMapper.selectList(new LambdaQueryWrapper<NewsTag>()
                .eq(NewsTag::getDeleted, 0).orderByAsc(NewsTag::getId));
        // 引用稿件数量实时统计（按 value_tags JSON 精确匹配标签名）
        for (NewsTag t : tags) {
            t.setRefCount(newsMapper.selectCount(new LambdaQueryWrapper<News>()
                    .eq(News::getDeleted, 0)
                    .like(News::getValueTags, "\"" + t.getTagName() + "\"")).intValue());
        }
        return tags;
    }

    /** 按名称精确查找（未删除） */
    public NewsTag find(String name) {
        if (name == null || name.isBlank()) return null;
        return tagMapper.selectOne(new LambdaQueryWrapper<NewsTag>()
                .eq(NewsTag::getDeleted, 0).eq(NewsTag::getTagName, name.trim())
                .last("LIMIT 1"));
    }

    @Transactional
    public NewsTag save(NewsTag tag) {
        CulRole.requireAny(CulRole.CULTURE_HQ);
        NewsFlow.require(tag.getTagName() != null && tag.getTagName().trim().length() >= 2
                && tag.getTagName().trim().length() <= 50, "13", "标签名称需2-50字");
        NewsFlow.require(tag.getTagScope() != null
                && ("集团全局".equals(tag.getTagScope()) || "子公司私有".equals(tag.getTagScope())),
                "13", "标签归属需为：集团全局/子公司私有");
        String name = tag.getTagName().trim();
        NewsTag dup = find(name);
        NewsFlow.require(dup == null || dup.getId().equals(tag.getId()),
                "13", "标签名称已存在：" + name);
        tag.setTagName(name);
        tag.setUpdateTime(LocalDateTime.now());
        if (tag.getId() == null) {
            tag.setEnabled(tag.getEnabled() == null ? 1 : tag.getEnabled());
            tag.setRefCount(0);
            tag.setCreator(CurrentHeader.operator());
            tag.setCreateTime(tag.getUpdateTime());
            tagMapper.insert(tag);
            auditService.log("NEW", "cul_news_tag", tag.getId(), CurrentHeader.operator() + " 新增新闻标签 " + name);
        } else {
            NewsTag old = tagMapper.selectById(tag.getId());
            NewsFlow.require(old != null && old.getDeleted() == 0, "13", "标签不存在");
            old.setTagName(name);
            old.setTagScope(tag.getTagScope());
            tagMapper.updateById(old);
            auditService.log("EDIT", "cul_news_tag", tag.getId(), CurrentHeader.operator() + " 编辑新闻标签 " + name);
        }
        return tag;
    }

    /** 启用/停用 */
    @Transactional
    public NewsTag toggle(Long id) {
        CulRole.requireAny(CulRole.CULTURE_HQ);
        NewsTag t = tagMapper.selectById(id);
        NewsFlow.require(t != null && t.getDeleted() == 0, "13", "标签不存在");
        t.setEnabled(t.getEnabled() != null && t.getEnabled() == 1 ? 0 : 1);
        t.setUpdateTime(LocalDateTime.now());
        tagMapper.updateById(t);
        auditService.log("TOGGLE", "cul_news_tag", id,
                CurrentHeader.operator() + (t.getEnabled() == 1 ? " 启用标签 " : " 停用标签 ") + t.getTagName());
        return t;
    }
}
