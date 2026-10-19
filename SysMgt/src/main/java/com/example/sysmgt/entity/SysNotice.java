package com.example.sysmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_notice")
public class SysNotice {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("notice_title")
    private String noticeTitle;

    @TableField("notice_type")
    private String noticeType;

    @TableField("notice_content")
    private String noticeContent;

    @TableField("status")
    private String status;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField("publisher")
    private String publisher;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
