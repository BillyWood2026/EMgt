package com.example.bimgt.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** BI报表设计 */
@Data
@TableName("bi_report")
public class BiReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportCode;
    private String reportName;
    private Long datasetId;
    /** 图表类型:表格/柱状/折线/饼图/交叉表 */
    private String chartType;
    /** 布局JSON */
    private String layoutJson;
    /** 状态:草稿/已发布 */
    private String status;
    private LocalDateTime publishTime;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
