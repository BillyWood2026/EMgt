package com.example.culturemgt.common.cul;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * CCMS 企业文化角色常量与鉴权工具（对标需求 IAM 角色矩阵）。
 * C01 普通员工；C02 子公司文化专员；C03 集团文化管理员；
 * C04 部门负责人/干部；C05 人力负责人；C06 法务合规；C07 分管高管；C99 超级管理员。
 * 角色由请求头 X-Culture-Role 传入（逗号分隔多个），缺失时按 C01 普通员工处理。
 */
public final class CulRole {

    public static final String C01 = "C01"; // 普通员工
    public static final String C02 = "C02"; // 子公司文化专员
    public static final String C03 = "C03"; // 集团文化管理员
    public static final String C04 = "C04"; // 部门负责人/干部
    public static final String C05 = "C05"; // 人力负责人
    public static final String C06 = "C06"; // 法务合规
    public static final String C07 = "C07"; // 分管高管
    public static final String C99 = "C99"; // 超级管理员

    public static final String HEADER = "X-Culture-Role";

    /** 全员可读 */
    public static final String[] READ = {C01, C02, C03, C04, C05, C06, C07, C99};

    /** 文化管理岗（集团/子公司文化专员+超管），日常业务维护 */
    public static final String[] CULTURE_STAFF = {C02, C03, C99};

    /** 集团级文化管理（终审/发布/全局配置） */
    public static final String[] CULTURE_HQ = {C03, C99};

    /** 审批岗：部门负责人/文化专员/人力/法务/高管/超管 */
    public static final String[] APPROVER = {C02, C03, C04, C05, C06, C07, C99};

    /** 高管终审 */
    public static final String[] EXEC = {C07, C99};

    /** 管理端可见（非普通员工） */
    public static final String[] BACKSTAGE = {C02, C03, C04, C05, C06, C07, C99};

    private CulRole() {
    }

    public static Set<String> currentRoles() {
        String header = CurrentHeader.get(HEADER);
        if (header == null || header.isBlank()) {
            return Collections.singleton(C01);
        }
        Set<String> roles = new HashSet<>();
        for (String r : header.split(",")) {
            String t = r.trim().toUpperCase();
            if (!t.isEmpty()) {
                roles.add(t);
            }
        }
        if (roles.isEmpty()) {
            roles.add(C01);
        }
        return roles;
    }

    public static boolean hasAny(String... required) {
        Set<String> roles = currentRoles();
        return Arrays.stream(required).anyMatch(roles::contains);
    }

    /** 写操作鉴权，不满足抛 403。 */
    public static void requireAny(String... required) {
        if (!hasAny(required)) {
            throw new CulAuthException("无操作权限，当前角色：" + currentRoles()
                    + "，需要：" + Arrays.toString(required));
        }
    }
}
