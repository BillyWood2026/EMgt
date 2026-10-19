package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 报告模板配置（需求 8.5）。 */
@Data
@TableName("opinion_report_template")
public class OpinionReportTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 日报/周报/月报/专报 */
    private String reportType;
    private String sections;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
