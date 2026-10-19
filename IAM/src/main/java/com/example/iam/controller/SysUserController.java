package com.example.iam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.iam.entity.rbac.IamRole;
import com.example.iam.entity.user.IamUser;
import com.example.iam.mapper.IamRoleMapper;
import com.example.iam.mapper.IamUserMapper;
import com.example.iam.mapper.IamUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final IamUserMapper userMapper;
    private final IamRoleMapper roleMapper;
    private final IamUserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String status) {

        LambdaQueryWrapper<IamUser> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(IamUser::getUsername, username);
        }
        if (realName != null && !realName.isEmpty()) {
            wrapper.like(IamUser::getRealName, realName);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(IamUser::getStatus, status.toUpperCase());
        }
        wrapper.orderByDesc(IamUser::getCreatedTime);

        Page<IamUser> page = new Page<>(pageNum, pageSize);
        Page<IamUser> result = userMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = result.getRecords().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("realName", user.getRealName());
            map.put("email", user.getEmail());
            map.put("phone", user.getMobile());
            map.put("employeeId", user.getEmployeeId());
            map.put("status", user.getStatus());
            map.put("userType", user.getUserType());
            map.put("createTime", user.getCreatedTime());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return buildSuccessResponse(data);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        IamUser user = userMapper.selectById(id);
        if (user == null) {
            return buildErrorResponse("用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("email", user.getEmail());
        data.put("phone", user.getMobile());
        data.put("employeeId", user.getEmployeeId());
        data.put("status", user.getStatus());
        data.put("userType", user.getUserType());
        data.put("roleId", user.getDefaultRoleId());
        data.put("orgId", user.getDefaultOrgId());
        return buildSuccessResponse(data);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        String realName = (String) request.get("realName");
        String email = (String) request.get("email");
        String phone = (String) request.get("phone");
        Long roleId = request.get("roleId") != null ? Long.valueOf(request.get("roleId").toString()) : null;

        IamUser existing = userMapper.selectByUsername(username);
        if (existing != null) {
            return buildErrorResponse("用户名已存在");
        }

        IamUser user = new IamUser();
        user.setEmployeeId("EMP" + System.currentTimeMillis());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password != null ? password : "123456"));
        user.setRealName(realName);
        user.setEmail(email);
        user.setMobile(phone);
        user.setStatus("ACTIVE");
        user.setUserType("EMPLOYEE");
        user.setDefaultRoleId(roleId);
        user.setCreatedBy("SYSTEM");
        user.setCreatedTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("User created: {}", username);
        return buildSuccessResponse("创建成功", null);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        IamUser user = userMapper.selectById(id);
        if (user == null) {
            return buildErrorResponse("用户不存在");
        }

        if (request.containsKey("realName")) {
            user.setRealName((String) request.get("realName"));
        }
        if (request.containsKey("email")) {
            user.setEmail((String) request.get("email"));
        }
        if (request.containsKey("phone")) {
            user.setMobile((String) request.get("phone"));
        }
        if (request.containsKey("status")) {
            user.setStatus(((String) request.get("status")).toUpperCase());
        }
        if (request.containsKey("roleId")) {
            user.setDefaultRoleId(Long.valueOf(request.get("roleId").toString()));
        }
        user.setUpdatedBy("SYSTEM");
        user.setUpdatedTime(LocalDateTime.now());

        userMapper.updateById(user);
        return buildSuccessResponse("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        IamUser user = userMapper.selectById(id);
        if (user == null) {
            return buildErrorResponse("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            return buildErrorResponse("不能删除系统管理员");
        }
        userMapper.deleteById(id);
        return buildSuccessResponse("删除成功", null);
    }

    @PutMapping("/{id}/password")
    public Map<String, Object> resetPassword(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String newPassword = (String) request.get("newPassword");
        IamUser user = userMapper.selectById(id);
        if (user == null) {
            return buildErrorResponse("用户不存在");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedBy("SYSTEM");
        user.setUpdatedTime(LocalDateTime.now());
        userMapper.updateById(user);
        return buildSuccessResponse("密码重置成功", null);
    }

    @GetMapping("/roles")
    public Map<String, Object> getRoleList() {
        List<IamRole> roles = roleMapper.selectList(null);
        List<Map<String, Object>> list = roles.stream().map(role -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", role.getId());
            map.put("name", role.getRoleName());
            map.put("code", role.getRoleCode());
            return map;
        }).collect(Collectors.toList());
        return buildSuccessResponse(list);
    }

    private Map<String, Object> buildSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", message);
        return response;
    }
}
