package com.example.partyaffairsmgt.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.system.entity.SysDictItem;
import com.example.partyaffairsmgt.system.entity.SysDictType;
import com.example.partyaffairsmgt.system.mapper.SysDictItemMapper;
import com.example.partyaffairsmgt.system.mapper.SysDictTypeMapper;
import com.example.partyaffairsmgt.system.service.SysAuditWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 数据字典（需求 12.3：修改记版本，被业务引用禁物理删除）。
 * E-SY-09 字典类型编码重复；E-SY-10 字典值被引用禁删(引导停用)；E-SY-16 必填缺失。
 */
@RestController
@RequestMapping("/api/party/system/dict")
@CrossOrigin(origins = "*")
public class SysDictController {

    /** 字典类型 -> 业务引用检查 SQL（参数为 item_value）。 */
    private static final Map<String, String> REF_SQL = Map.of(
            "ACTIVITY_TYPE", "SELECT COUNT(*) FROM activity_record WHERE activity_type = ? AND deleted = 0",
            "ORG_TYPE", "SELECT COUNT(*) FROM party_org WHERE org_type = ? AND deleted = 0",
            "NEWS_STATUS", "SELECT COUNT(*) FROM news_article WHERE status = ? AND deleted = 0",
            "ASSESS_GRADE", "SELECT COUNT(*) FROM assess_task WHERE grade = ? AND deleted = 0");

    @Autowired
    private SysDictTypeMapper typeMapper;
    @Autowired
    private SysDictItemMapper itemMapper;
    @Autowired
    private SysAuditWriter auditWriter;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ---------------- 类型 ----------------

    @GetMapping("/type/list")
    public ResponseResult<List<SysDictType>> typeList() {
        PartyRole.requireAny(PartyRole.READ);
        QueryWrapper<SysDictType> qw = new QueryWrapper<>();
        qw.orderByAsc("type_code");
        return ResponseResult.success(typeMapper.selectList(qw));
    }

    @PostMapping("/type")
    public ResponseResult<SysDictType> createType(@RequestBody SysDictType type) {
        PartyRole.requireAny(PartyRole.R02);
        if (type.getTypeCode() == null || type.getTypeCode().isBlank()
                || type.getTypeName() == null || type.getTypeName().isBlank()) {
            throw PartyBizException.block("E-SY-16", "必填项缺失：类型编码/类型名称");
        }
        QueryWrapper<SysDictType> qw = new QueryWrapper<>();
        qw.eq("type_code", type.getTypeCode());
        if (typeMapper.selectCount(qw) > 0) {
            throw PartyBizException.block("E-SY-09", "字典类型编码重复：" + type.getTypeCode());
        }
        LocalDateTime now = LocalDateTime.now();
        type.setId(null);
        type.setCreateTime(now);
        type.setUpdateTime(now);
        typeMapper.insert(type);
        auditWriter.write("操作", "新增字典类型", "sys_dict_type", type.getId(), null, type.getTypeCode(), null);
        return ResponseResult.success(type);
    }

    @PutMapping("/type/{id}")
    public ResponseResult<SysDictType> updateType(@PathVariable Long id, @RequestBody SysDictType type) {
        PartyRole.requireAny(PartyRole.R02);
        type.setId(id);
        type.setTypeCode(null);
        type.setCreateTime(null);
        type.setUpdateTime(LocalDateTime.now());
        typeMapper.updateById(type);
        auditWriter.write("操作", "修改字典类型", "sys_dict_type", id, null, type.getTypeName(), null);
        return ResponseResult.success(typeMapper.selectById(id));
    }

    @DeleteMapping("/type/{id}")
    public ResponseResult<Boolean> deleteType(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        SysDictType type = typeMapper.selectById(id);
        if (type == null) throw PartyBizException.block("E-SY-16", "字典类型不存在：" + id);
        QueryWrapper<SysDictItem> qw = new QueryWrapper<>();
        qw.eq("type_code", type.getTypeCode());
        if (itemMapper.selectCount(qw) > 0) {
            throw PartyBizException.block("E-SY-16", "类型下存在字典项，请先清空：" + type.getTypeCode());
        }
        boolean ok = typeMapper.deleteById(id) > 0;
        auditWriter.write("操作", "删除字典类型", "sys_dict_type", id, type.getTypeCode(), null, null);
        return ResponseResult.success(ok);
    }

    // ---------------- 字典项 ----------------

    @GetMapping("/item/list")
    public ResponseResult<List<SysDictItem>> itemList(@RequestParam String typeCode,
                                                      @RequestParam(required = false) String status) {
        PartyRole.requireAny(PartyRole.READ);
        QueryWrapper<SysDictItem> qw = new QueryWrapper<>();
        qw.eq("type_code", typeCode);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        qw.orderByAsc("sort_order", "id");
        return ResponseResult.success(itemMapper.selectList(qw));
    }

    @PostMapping("/item")
    public ResponseResult<SysDictItem> createItem(@RequestBody SysDictItem item) {
        PartyRole.requireAny(PartyRole.R02);
        if (item.getTypeCode() == null || item.getItemValue() == null || item.getItemLabel() == null) {
            throw PartyBizException.block("E-SY-16", "必填项缺失：类型编码/字典值/标签");
        }
        QueryWrapper<SysDictItem> qw = new QueryWrapper<>();
        qw.eq("type_code", item.getTypeCode()).eq("item_value", item.getItemValue());
        if (itemMapper.selectCount(qw) > 0) {
            throw PartyBizException.block("E-SY-16", "字典值已存在：" + item.getItemValue());
        }
        LocalDateTime now = LocalDateTime.now();
        item.setId(null);
        if (item.getStatus() == null || item.getStatus().isBlank()) item.setStatus("启用");
        item.setVersion(1);
        item.setCreateTime(now);
        item.setUpdateTime(now);
        itemMapper.insert(item);
        auditWriter.write("操作", "新增字典项", "sys_dict_item", item.getId(), null,
                item.getTypeCode() + "/" + item.getItemValue(), null);
        return ResponseResult.success(item);
    }

    @PutMapping("/item/{id}")
    public ResponseResult<SysDictItem> updateItem(@PathVariable Long id, @RequestBody SysDictItem item) {
        PartyRole.requireAny(PartyRole.R02);
        SysDictItem old = itemMapper.selectById(id);
        if (old == null) throw PartyBizException.block("E-SY-16", "字典项不存在：" + id);
        item.setId(id);
        item.setTypeCode(null);
        item.setCreateTime(null);
        item.setVersion((old.getVersion() == null ? 1 : old.getVersion()) + 1);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
        auditWriter.write("操作", "修改字典项", "sys_dict_item", id,
                old.getItemLabel() + "(v" + old.getVersion() + ")",
                item.getItemLabel() + "(v" + item.getVersion() + ")", null);
        return ResponseResult.success(itemMapper.selectById(id));
    }

    /** 删除：被业务引用则阻断并引导停用，否则物理删除。 */
    @DeleteMapping("/item/{id}")
    public ResponseResult<Boolean> deleteItem(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        SysDictItem item = itemMapper.selectById(id);
        if (item == null) throw PartyBizException.block("E-SY-16", "字典项不存在：" + id);
        long refs = countReferences(item.getTypeCode(), item.getItemValue());
        if (refs > 0) {
            throw PartyBizException.block("E-SY-10",
                    "字典值已被业务引用 " + refs + " 次，禁止物理删除，请改用停用：" + item.getItemValue());
        }
        boolean ok = itemMapper.physicalDelete(id) > 0;
        auditWriter.write("敏感", "删除字典项", "sys_dict_item", id,
                item.getTypeCode() + "/" + item.getItemValue(), null, null);
        return ResponseResult.success(ok);
    }

    /** 停用（被引用字典项的唯一处置方式）。 */
    @PostMapping("/item/{id}/disable")
    public ResponseResult<SysDictItem> disableItem(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        SysDictItem old = itemMapper.selectById(id);
        if (old == null) throw PartyBizException.block("E-SY-16", "字典项不存在：" + id);
        SysDictItem u = new SysDictItem();
        u.setId(id);
        u.setStatus("停用");
        u.setVersion((old.getVersion() == null ? 1 : old.getVersion()) + 1);
        u.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(u);
        auditWriter.write("操作", "停用字典项", "sys_dict_item", id,
                old.getItemValue() + "/" + old.getStatus(), "停用", null);
        return ResponseResult.success(itemMapper.selectById(id));
    }

    private long countReferences(String typeCode, String itemValue) {
        String sql = REF_SQL.get(typeCode);
        if (sql == null) return 0;
        try {
            Long c = jdbcTemplate.queryForObject(sql, Long.class, itemValue);
            return c == null ? 0 : c;
        } catch (Exception e) {
            return 0; // 目标表不存在时按无引用处理
        }
    }
}
