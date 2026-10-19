package com.example.bimgt.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** BI图表组件库 */
@Data
@TableName("bi_chart")
public class BiChart {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String chartName;
    /** 类型:柱状/折线/饼图/表格/指标卡 */
    private String chartType;
    /** 配置模板JSON */
    private String configTemplate;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
