package com.example.culturemgt.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.activity.entity.CultureMaterial;
import com.example.culturemgt.activity.entity.MaterialFlow;
import com.example.culturemgt.activity.mapper.CultureMaterialMapper;
import com.example.culturemgt.activity.mapper.MaterialFlowMapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 文化物料库存与出入库台账；库存≤安全阈值的预警由前端按 stock/safety_stock 计算。 */
@Service
public class MaterialService {

    @Autowired private CultureMaterialMapper matMapper;
    @Autowired private MaterialFlowMapper flowMapper;
    @Autowired private AuditService audit;

    public List<CultureMaterial> list(String status, String type) {
        LambdaQueryWrapper<CultureMaterial> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) w.eq(CultureMaterial::getStatus, status);
        if (type != null && !type.isBlank()) w.eq(CultureMaterial::getMatType, type);
        w.orderByAsc(CultureMaterial::getId);
        return matMapper.selectList(w);
    }

    public List<MaterialFlow> flows(Long materialId) {
        LambdaQueryWrapper<MaterialFlow> w = new LambdaQueryWrapper<>();
        if (materialId != null) w.eq(MaterialFlow::getMaterialId, materialId);
        w.orderByDesc(MaterialFlow::getId);
        return flowMapper.selectList(w);
    }

    public CultureMaterial save(CultureMaterial m) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        int n = ActivityFlow.len(m.getMatName());
        ActivityFlow.require(n >= 2 && n <= 100, "01", "物料名称需2-100字");
        ActivityFlow.require(m.getStock() == null || m.getStock() >= 0, "01", "库存须≥0");
        ActivityFlow.require(m.getSafetyStock() == null || m.getSafetyStock() >= 0, "01", "安全库存须≥0");
        if (m.getId() == null) {
            m.setStatus(m.getStatus() == null ? "正常" : m.getStatus());
            m.setCreateTime(LocalDateTime.now());
            matMapper.insert(m);
            audit.log("NEW", "cul_material", m.getId(), CurrentHeader.operator() + " 新增物料 " + m.getMatName());
        } else {
            CultureMaterial old = matMapper.selectById(m.getId());
            ActivityFlow.require(old != null, "01", "物料不存在");
            m.setStatus(old.getStatus());
            m.setUpdateTime(LocalDateTime.now());
            matMapper.updateById(m);
            audit.log("EDIT", "cul_material", m.getId(), "调整物料 " + m.getMatName());
        }
        return m;
    }

    /** 出入库：写台账并回写库存，balance_after 记录操作后库存；出库不足拦截。 */
    @Transactional
    public void flow(Long materialId, String flowType, int qty, Long activityId, String reason) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CultureMaterial m = matMapper.selectById(materialId);
        ActivityFlow.require(m != null, "01", "物料不存在");
        ActivityFlow.require("入库".equals(flowType) || "出库".equals(flowType), "01", "流水类型须为 入库/出库");
        ActivityFlow.require(qty > 0, "01", "数量须为正整数");
        int balance = m.getStock() == null ? 0 : m.getStock();
        if ("出库".equals(flowType)) {
            ActivityFlow.require(balance >= qty, "02", "库存不足：当前" + balance + " " + m.getUnit() + "，需" + qty);
            balance -= qty;
        } else {
            balance += qty;
        }
        CultureMaterial u = new CultureMaterial();
        u.setId(materialId);
        u.setStock(balance);
        u.setUpdateTime(LocalDateTime.now());
        matMapper.updateById(u);

        MaterialFlow f = new MaterialFlow();
        f.setMaterialId(materialId);
        f.setActivityId(activityId);
        f.setFlowType(flowType);
        f.setQty(qty);
        f.setBalanceAfter(balance);
        f.setOperator(CurrentHeader.operator());
        f.setReason(reason);
        f.setCreateTime(LocalDateTime.now());
        flowMapper.insert(f);
        audit.log("FLOW", "cul_material", materialId, m.getMatName() + " " + flowType + qty + m.getUnit() + "，结余" + balance);
    }

    /** 正常/停用切换。 */
    public void toggle(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        CultureMaterial m = matMapper.selectById(id);
        ActivityFlow.require(m != null, "01", "物料不存在");
        CultureMaterial u = new CultureMaterial();
        u.setId(id);
        u.setStatus("正常".equals(m.getStatus()) ? "停用" : "正常");
        u.setUpdateTime(LocalDateTime.now());
        matMapper.updateById(u);
        audit.log("TOGGLE", "cul_material", id, "物料状态切换为" + u.getStatus());
    }
}
