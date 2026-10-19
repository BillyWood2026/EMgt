package com.example.partyaffairsmgt.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.system.entity.SysRole;
import com.example.partyaffairsmgt.system.entity.SysUser;
import com.example.partyaffairsmgt.system.mapper.SysRoleMapper;
import com.example.partyaffairsmgt.system.mapper.SysUserMapper;
import com.example.partyaffairsmgt.system.service.SysAuditWriter;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统角色管理（需求 12.1：页面/按钮/字段/数据范围四级权限）。
 * E-SY-06 角色编码重复；E-SY-07 角色被用户引用禁删；E-SY-16 必填缺失。
 */
@RestController
@RequestMapping("/api/party/system/role")
@CrossOrigin(origins = "*")
public class SysRoleController {

    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysAuditWriter auditWriter;

    @GetMapping("/list")
    public ResponseResult<Page<SysRole>> list(@RequestParam(required = false) String roleCodeLike,
                                              @RequestParam(required = false) String roleNameLike,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "20") Integer pageSize) {
        PartyRole.requireAny(PartyRole.READ);
        QueryWrapper<SysRole> qw = new QueryWrapper<>();
        if (roleCodeLike != null && !roleCodeLike.isBlank()) qw.like("role_code", roleCodeLike);
        if (roleNameLike != null && !roleNameLike.isBlank()) qw.like("role_name", roleNameLike);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        qw.orderByAsc("role_code");
        return ResponseResult.success(roleMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @PostMapping
    public ResponseResult<SysRole> create(@RequestBody SysRole role) {
        PartyRole.requireAny(PartyRole.R02);
        if (role.getRoleCode() == null || role.getRoleCode().isBlank()
                || role.getRoleName() == null || role.getRoleName().isBlank()) {
            throw PartyBizException.block("E-SY-16", "必填项缺失：角色编码/角色名称");
        }
        QueryWrapper<SysRole> qw = new QueryWrapper<>();
        qw.eq("role_code", role.getRoleCode());
        if (roleMapper.selectCount(qw) > 0) {
            throw PartyBizException.block("E-SY-06", "角色编码重复：" + role.getRoleCode());
        }
        LocalDateTime now = LocalDateTime.now();
        role.setId(null);
        if (role.getStatus() == null || role.getStatus().isBlank()) role.setStatus("启用");
        role.setCreateTime(now);
        role.setUpdateTime(now);
        roleMapper.insert(role);
        auditWriter.write("操作", "新增角色", "sys_role", role.getId(), null, role.getRoleCode(), null);
        return ResponseResult.success(role);
    }

    @PutMapping("/{id}")
    public ResponseResult<SysRole> update(@PathVariable Long id, @RequestBody SysRole role) {
        PartyRole.requireAny(PartyRole.R02);
        SysRole old = roleMapper.selectById(id);
        if (old == null) throw PartyBizException.block("E-SY-16", "角色不存在：" + id);
        role.setId(id);
        role.setRoleCode(null);
        role.setCreateTime(null);
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
        SysRole cur = roleMapper.selectById(id);
        auditWriter.write("敏感", "权限变更", "sys_role", id, toJson(old), toJson(cur), null);
        return ResponseResult.success(cur);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        SysRole old = roleMapper.selectById(id);
        if (old == null) throw PartyBizException.block("E-SY-16", "角色不存在：" + id);
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        qw.like("role_codes", old.getRoleCode());
        long refs = userMapper.selectCount(qw);
        if (refs > 0) {
            throw PartyBizException.block("E-SY-07",
                    "角色已被 " + refs + " 个用户引用，禁止删除：" + old.getRoleCode());
        }
        boolean ok = roleMapper.deleteById(id) > 0;
        auditWriter.write("敏感", "删除角色", "sys_role", id, toJson(old), null, null);
        return ResponseResult.success(ok);
    }

    private static String toJson(SysRole r) {
        if (r == null) return null;
        return "{\"roleCode\":\"" + r.getRoleCode() + "\",\"roleName\":\"" + r.getRoleName()
                + "\",\"pagePerms\":" + r.getPagePerms() + ",\"buttonPerms\":" + r.getButtonPerms()
                + ",\"fieldPerms\":" + r.getFieldPerms() + ",\"dataScope\":\"" + r.getDataScope()
                + "\",\"status\":\"" + r.getStatus() + "\"}";
    }
}
