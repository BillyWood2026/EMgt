package com.example.partyaffairsmgt.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.system.entity.SysUser;
import com.example.partyaffairsmgt.system.mapper.SysUserMapper;
import com.example.partyaffairsmgt.system.service.SysAuditWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统用户管理（需求 12.1）。
 * E-SY-01 账号已存在；E-SY-02 密码不合策略；E-SY-03 密码超90天未换(预警)；
 * E-SY-04 失败5次锁15分钟；E-SY-05 账号停用/锁定；E-SY-16 必填缺失。
 */
@RestController
@RequestMapping("/api/party/system/user")
@CrossOrigin(origins = "*")
public class SysUserController {

    /** 密码策略：>=8位，含大小写字母、数字、特殊字符 */
    private static final String PWD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";
    private static final int FAIL_LOCK_COUNT = 5;
    private static final int FAIL_LOCK_MINUTES = 15;
    private static final int PWD_MAX_AGE_DAYS = 90;

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysAuditWriter auditWriter;

    @GetMapping("/list")
    public ResponseResult<Page<SysUser>> list(@RequestParam(required = false) String accountLike,
                                              @RequestParam(required = false) String nameLike,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) Long orgId,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PartyRole.requireAny(PartyRole.READ);
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        if (accountLike != null && !accountLike.isBlank()) qw.like("account", accountLike);
        if (nameLike != null && !nameLike.isBlank()) qw.like("name", nameLike);
        if (status != null && !status.isBlank()) qw.eq("status", status);
        if (orgId != null) qw.eq("org_id", orgId);
        qw.orderByDesc("id");
        return ResponseResult.success(userMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @PostMapping
    public ResponseResult<SysUser> create(@RequestBody SysUser user) {
        PartyRole.requireAny(PartyRole.R02);
        validateRequired(user.getAccount(), user.getName());
        String password = user.getPwdHash(); // 前端明文传 password 字段放 pwdHash 位，入库前转哈希
        if (password == null || password.isBlank()) {
            throw PartyBizException.block("E-SY-16", "必填项缺失：初始密码");
        }
        checkAccountUnique(user.getAccount(), null);
        checkPasswordPolicy(password);
        LocalDateTime now = LocalDateTime.now();
        user.setId(null);
        user.setPwdHash(sha256(user.getAccount() + ":" + password));
        user.setPwdUpdateTime(now);
        user.setFailCount(0);
        if (user.getStatus() == null || user.getStatus().isBlank()) user.setStatus("启用");
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);
        auditWriter.write("操作", "新增用户", "sys_user", user.getId(), null, user.getAccount(), null);
        user.setPwdHash(null);
        return ResponseResult.success(user);
    }

    @PutMapping("/{id}")
    public ResponseResult<SysUser> update(@PathVariable Long id, @RequestBody SysUser user) {
        PartyRole.requireAny(PartyRole.R02);
        SysUser old = userMapper.selectById(id);
        if (old == null) throw PartyBizException.block("E-SY-16", "用户不存在：" + id);
        user.setId(id);
        user.setAccount(null);
        user.setPwdHash(null);
        user.setPwdUpdateTime(null);
        user.setFailCount(null);
        user.setLockUntil(null);
        user.setLastLoginTime(null);
        user.setCreateTime(null);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        auditWriter.write("操作", "修改用户", "sys_user", id, old.getName(), user.getName(), null);
        SysUser cur = userMapper.selectById(id);
        cur.setPwdHash(null);
        return ResponseResult.success(cur);
    }

    /** 启用/停用切换。 */
    @PostMapping("/{id}/toggle")
    public ResponseResult<SysUser> toggle(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        SysUser old = userMapper.selectById(id);
        if (old == null) throw PartyBizException.block("E-SY-16", "用户不存在：" + id);
        String next = "启用".equals(old.getStatus()) ? "停用" : "启用";
        SysUser u = new SysUser();
        u.setId(id);
        u.setStatus(next);
        u.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(u);
        auditWriter.write("敏感", "用户启停", "sys_user", id, old.getStatus(), next, null);
        SysUser cur = userMapper.selectById(id);
        cur.setPwdHash(null);
        return ResponseResult.success(cur);
    }

    /** 重置密码为 账号首字母大写+@12345。 */
    @PostMapping("/{id}/reset-pwd")
    public ResponseResult<Map<String, Object>> resetPwd(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        SysUser old = userMapper.selectById(id);
        if (old == null) throw PartyBizException.block("E-SY-16", "用户不存在：" + id);
        String newPwd = capitalize(old.getAccount()) + "@12345";
        SysUser u = new SysUser();
        u.setId(id);
        u.setPwdHash(sha256(old.getAccount() + ":" + newPwd));
        u.setPwdUpdateTime(LocalDateTime.now());
        u.setFailCount(0);
        u.setLockUntil(null);
        u.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(u);
        auditWriter.write("敏感", "重置密码", "sys_user", id, null, null, "账号=" + old.getAccount());
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", id);
        r.put("newPassword", newPwd);
        return ResponseResult.success(r);
    }

    /** 批量导入：逐行校验，返回成功/失败明细。 */
    @PostMapping("/import")
    public ResponseResult<Map<String, Object>> importUsers(@RequestBody List<SysUser> users) {
        PartyRole.requireAny(PartyRole.R02);
        int ok = 0;
        List<String> errors = new ArrayList<>();
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                SysUser u = users.get(i);
                int row = i + 1;
                try {
                    validateRequired(u.getAccount(), u.getName());
                    String pwd = u.getPwdHash();
                    if (pwd == null || pwd.isBlank()) {
                        throw PartyBizException.block("E-SY-16", "必填项缺失：初始密码");
                    }
                    checkAccountUnique(u.getAccount(), null);
                    checkPasswordPolicy(pwd);
                    LocalDateTime now = LocalDateTime.now();
                    u.setId(null);
                    u.setPwdHash(sha256(u.getAccount() + ":" + pwd));
                    u.setPwdUpdateTime(now);
                    u.setFailCount(0);
                    if (u.getStatus() == null || u.getStatus().isBlank()) u.setStatus("启用");
                    u.setCreateTime(now);
                    u.setUpdateTime(now);
                    userMapper.insert(u);
                    ok++;
                } catch (PartyBizException e) {
                    errors.add("第" + row + "行(" + u.getAccount() + ")：" + e.getMessage());
                }
            }
        }
        auditWriter.write("操作", "批量导入用户", "sys_user", null, null,
                "ok=" + ok + ",fail=" + errors.size(), null);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ok", ok);
        r.put("fail", errors.size());
        r.put("errors", errors);
        return ResponseResult.success(r);
    }

    /** 登录（公开接口，无角色鉴权）。 */
    @PostMapping("/login")
    public ResponseResult<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String account = body.get("account");
        String password = body.get("password");
        if (account == null || account.isBlank() || password == null || password.isBlank()) {
            throw PartyBizException.block("E-SY-16", "必填项缺失：账号或密码");
        }
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        qw.eq("account", account);
        SysUser user = userMapper.selectOne(qw);
        if (user == null) {
            return ResponseResult.error("账号或密码错误");
        }
        LocalDateTime now = LocalDateTime.now();
        if ("停用".equals(user.getStatus())) {
            throw PartyBizException.block("E-SY-05", "账号已停用，请联系管理员：" + account);
        }
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(now)) {
            throw PartyBizException.block("E-SY-04", "账号已锁定至 " + user.getLockUntil()
                    + "（登录失败" + FAIL_LOCK_COUNT + "次锁" + FAIL_LOCK_MINUTES + "分钟）");
        }
        String expect = sha256(account + ":" + password);
        if (!expect.equalsIgnoreCase(user.getPwdHash() == null ? "" : user.getPwdHash())) {
            int fails = (user.getFailCount() == null ? 0 : user.getFailCount()) + 1;
            SysUser u = new SysUser();
            u.setId(user.getId());
            u.setUpdateTime(now);
            if (fails >= FAIL_LOCK_COUNT) {
                u.setFailCount(fails);
                u.setLockUntil(now.plusMinutes(FAIL_LOCK_MINUTES));
                u.setStatus("锁定");
                userMapper.updateById(u);
                auditWriter.write("登录", "登录锁定", "sys_user", user.getId(), null, null,
                        "账号=" + account + " 连续失败" + fails + "次");
                throw PartyBizException.block("E-SY-04",
                        "登录失败" + FAIL_LOCK_COUNT + "次，账号锁定" + FAIL_LOCK_MINUTES + "分钟");
            }
            u.setFailCount(fails);
            userMapper.updateById(u);
            return ResponseResult.error("账号或密码错误，剩余尝试次数：" + (FAIL_LOCK_COUNT - fails));
        }
        // 登录成功：清零失败计数、解除锁定、记录登录时间
        SysUser u = new SysUser();
        u.setId(user.getId());
        u.setFailCount(0);
        u.setLockUntil(null);
        if ("锁定".equals(user.getStatus())) u.setStatus("启用");
        u.setLastLoginTime(now);
        u.setUpdateTime(now);
        userMapper.updateById(u);
        auditWriter.write("登录", "登录成功", "sys_user", user.getId(), null, null, "账号=" + account);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", user.getId());
        r.put("account", user.getAccount());
        r.put("name", user.getName());
        r.put("roleCodes", user.getRoleCodes());
        r.put("orgId", user.getOrgId());
        r.put("orgName", user.getOrgName());
        r.put("lastLoginTime", now.toString());
        if (user.getPwdUpdateTime() != null
                && user.getPwdUpdateTime().plusDays(PWD_MAX_AGE_DAYS).isBefore(now)) {
            r.put("pwdWarn", "[E-SY-03][预警] 密码已超" + PWD_MAX_AGE_DAYS + "天未更换，请尽快修改密码");
        }
        return ResponseResult.success(r);
    }

    private void validateRequired(String account, String name) {
        if (account == null || account.isBlank() || name == null || name.isBlank()) {
            throw PartyBizException.block("E-SY-16", "必填项缺失：账号/姓名");
        }
    }

    private void checkAccountUnique(String account, Long excludeId) {
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        qw.eq("account", account);
        if (excludeId != null) qw.ne("id", excludeId);
        if (userMapper.selectCount(qw) > 0) {
            throw PartyBizException.block("E-SY-01", "账号已存在：" + account);
        }
    }

    private void checkPasswordPolicy(String password) {
        if (!password.matches(PWD_PATTERN)) {
            throw PartyBizException.block("E-SY-02", "密码不合策略：至少8位且含大小写字母、数字、特殊字符");
        }
    }

    static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
