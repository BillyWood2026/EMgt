package com.example.partyaffairsmgt.common.party;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 党务角色常量与鉴权工具。
 * R02 集团党委管理员；R03 上级党委审批；R04 党委组织委员；R07 党支部书记；
 * R08 支部组织委员（党员发展、党费经办）；R12 党费财务（会计/出纳）；
 * R13 新闻编辑（撰稿投稿）；R99 普通党员（只读）。
 * 角色由请求头 X-Party-Role 传入（逗号分隔多个），缺失时按 R99 只读处理。
 */
public final class PartyRole {

    public static final String R02 = "R02";
    public static final String R03 = "R03";
    public static final String R04 = "R04";
    public static final String R06 = "R06";
    public static final String R07 = "R07";
    public static final String R08 = "R08";
    public static final String R12 = "R12";
    public static final String R13 = "R13";
    public static final String R14 = "R14";
    public static final String R20 = "R20";
    public static final String R21 = "R21";
    public static final String R22 = "R22";
    public static final String R23 = "R23";
    public static final String R24 = "R24";
    public static final String R99 = "R99";

    public static final String HEADER = "X-Party-Role";

    /** 只读角色集（含普通党员 R99），用于查询类接口 */
    public static final String[] READ = {R02, R03, R04, R06, R07, R08, R99};

    /** 纪检监察域只读角色集（R06纪委领导/R20室主任/R21审查调查/R22审理/R23信访接待/监督专员/R24巡察专员） */
    public static final String[] DISCIPLINE_READ = {R02, R03, R04, R06, R20, R21, R22, R23, R24};

    /** 监察监督与党风廉政域只读角色集 */
    public static final String[] SUPERVISION_READ = {R02, R03, R04, R06, R07, R20, R23, R24};

    private PartyRole() {
    }

    public static Set<String> currentRoles() {
        String header = CurrentHeader.get(HEADER);
        if (header == null || header.isBlank()) {
            return Collections.singleton(R99);
        }
        Set<String> roles = new HashSet<>();
        for (String r : header.split(",")) {
            String t = r.trim().toUpperCase();
            if (!t.isEmpty()) {
                roles.add(t);
            }
        }
        if (roles.isEmpty()) {
            roles.add(R99);
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
            throw new PartyAuthException("无操作权限，当前角色：" + currentRoles()
                    + "，需要：" + Arrays.toString(required));
        }
    }
}
