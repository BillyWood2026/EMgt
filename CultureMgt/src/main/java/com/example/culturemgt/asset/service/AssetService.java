package com.example.culturemgt.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.asset.entity.CultureAsset;
import com.example.culturemgt.asset.mapper.CultureAssetMapper;
import com.example.culturemgt.common.cul.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AssetService {

    private final CultureAssetMapper assetMapper;
    private final AuditService auditService;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String[] TYPES = {"海报", "宣传片", "PPT模板", "文化手册", "文创物料", "其他"};

    public AssetService(CultureAssetMapper assetMapper, AuditService auditService) {
        this.assetMapper = assetMapper;
        this.auditService = auditService;
    }

    public List<CultureAsset> list(String assetType, String status, String keyword) {
        LambdaQueryWrapper<CultureAsset> w = new LambdaQueryWrapper<>();
        w.eq(CultureAsset::getDeleted, 0);
        if (assetType != null && !assetType.isBlank()) w.eq(CultureAsset::getAssetType, assetType);
        if (status != null && !status.isBlank()) w.eq(CultureAsset::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            w.and(x -> x.like(CultureAsset::getAssetName, keyword)
                        .or().like(CultureAsset::getAssetCode, keyword)
                        .or().like(CultureAsset::getTags, keyword));
        }
        w.orderByDesc(CultureAsset::getCreateTime);
        return assetMapper.selectList(w);
    }

    public CultureAsset get(Long id) {
        CultureAsset a = assetMapper.selectById(id);
        AssetFlow.require(a != null && a.getDeleted() == 0, "01", "资产不存在");
        return a;
    }

    @Transactional
    public CultureAsset save(CultureAsset a) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        validate(a);
        if (a.getId() != null) {
            CultureAsset old = assetMapper.selectById(a.getId());
            AssetFlow.require(old != null && old.getDeleted() == 0, "01", "资产不存在");
            a.setId(old.getId());
        } else {
            a.setAssetCode(nextCode());
            a.setStatus("启用");
            a.setCreator(CurrentHeader.operator());
            a.setDownloadCount(0);
            a.setViewCount(0);
        }
        a.setUpdateTime(LocalDateTime.now());
        if (a.getId() == null) {
            a.setCreateTime(LocalDateTime.now());
            assetMapper.insert(a);
        } else {
            assetMapper.updateById(a);
        }
        auditService.log(a.getId() == null ? "NEW" : "EDIT", "cul_culture_asset", a.getId(),
            (a.getId() == null ? "新建资产 " : "编辑资产 ") + a.getAssetCode());
        return a;
    }

    @Transactional
    public void toggle(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CultureAsset a = assetMapper.selectById(id);
        AssetFlow.require(a != null && a.getDeleted() == 0, "01", "资产不存在");
        a.setStatus("启用".equals(a.getStatus()) ? "停用" : "启用");
        a.setUpdateTime(LocalDateTime.now());
        assetMapper.updateById(a);
        auditService.log("TOGGLE", "cul_culture_asset", id,
            "切换资产状态 " + a.getAssetCode() + " → " + a.getStatus());
    }

    @Transactional
    public void delete(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CultureAsset a = assetMapper.selectById(id);
        AssetFlow.require(a != null && a.getDeleted() == 0, "01", "资产不存在");
        // deleted 带 @TableLogic，updateById 会剔除该字段，必须走 deleteById 触发逻辑删除
        assetMapper.deleteById(id);
        auditService.log("DELETE", "cul_culture_asset", id, "删除资产 " + a.getAssetCode());
    }

    @Transactional
    public CultureAsset view(Long id) {
        CultureAsset a = assetMapper.selectById(id);
        AssetFlow.require(a != null && a.getDeleted() == 0, "01", "资产不存在");
        a.setViewCount(a.getViewCount() + 1);
        a.setUpdateTime(LocalDateTime.now());
        assetMapper.updateById(a);
        return a;
    }

    @Transactional
    public CultureAsset download(Long id) {
        CultureAsset a = assetMapper.selectById(id);
        AssetFlow.require(a != null && a.getDeleted() == 0, "01", "资产不存在");
        a.setDownloadCount(a.getDownloadCount() + 1);
        a.setUpdateTime(LocalDateTime.now());
        assetMapper.updateById(a);
        return a;
    }

    private void validate(CultureAsset a) {
        AssetFlow.require(a.getAssetName() != null && a.getAssetName().trim().length() >= 2 && a.getAssetName().trim().length() <= 200, "02", "资产名称2-200字");
        AssetFlow.require(a.getAssetType() != null && Arrays.asList(TYPES).contains(a.getAssetType()), "03", "资产分类非法");
        AssetFlow.require(a.getFileUrl() != null && a.getFileUrl().trim().length() > 0, "04", "附件不能为空");
        AssetFlow.require(a.getOrgScope() != null && a.getOrgScope().trim().length() > 2, "05", "可下载组织不能为空");
    }

    private String nextCode() {
        String prefix = "ZC" + LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        long cnt = assetMapper.selectCount(
            new LambdaQueryWrapper<CultureAsset>()
                .likeRight(CultureAsset::getAssetCode, prefix)
                .eq(CultureAsset::getDeleted, 0));
        return prefix + String.format("%04d", cnt + 1);
    }
}
