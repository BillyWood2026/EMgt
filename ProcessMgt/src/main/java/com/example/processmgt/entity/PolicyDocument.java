package com.example.processmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 制度文档 Entity（起草→评审→审批→发布→修订）
 */
@Data
@TableName("policy_document")
public class PolicyDocument {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("doc_no")
    private String docNo;

    @TableField("title")
    private String title;

    @TableField("category")
    private String category;

    @TableField("content")
    private String content;

    /** draft/pending/approved/active/deprecated/archived */
    @TableField("status")
    private String status;

    @TableField("version")
    private Integer version;

    @TableField("owner_dept")
    private String ownerDept;

    @TableField("approver")
    private String approver;

    @TableField("approval_comment")
    private String approvalComment;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_DEPRECATED = "deprecated";
    public static final String STATUS_ARCHIVED = "archived";
}
