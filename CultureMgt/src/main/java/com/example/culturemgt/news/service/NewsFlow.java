package com.example.culturemgt.news.service;

import com.example.culturemgt.common.cul.CulBizException;

/**
 * 子模块9 新闻宣传管理 - 错误码与工具（E-NW-01~18）
 * 01稿件不存在 02标题非法 03摘要非法 04正文/素材非法 05新闻类型非法 06归属组织非法
 * 07状态不允许该操作 08仅撰稿本人可操作 09越权组织 10驳回意见非法 11下线原因必填
 * 12计划发布时间非法 13标签参数非法 14标签不存在或已停用 15置顶超上限 16无当前节点审批权限
 * 17版本不存在 18重复标题预警
 */
public class NewsFlow {

    public static final String EC = "E-NW";

    public static CulBizException block(String code, String msg) {
        return CulBizException.block(EC + "-" + code, msg);
    }

    public static CulBizException warn(String code, String msg) {
        return CulBizException.warn(EC + "-" + code, msg);
    }

    public static void require(boolean condition, String code, String msg) {
        if (!condition) {
            throw block(code, msg);
        }
    }
}
