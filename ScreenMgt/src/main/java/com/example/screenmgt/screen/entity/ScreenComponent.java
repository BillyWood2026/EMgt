package com.example.screenmgt.screen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 大屏可视化组件 */
@Data
@TableName("screen_component")
public class ScreenComponent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String componentName;
    /** 类型:数字卡片/折线图/柱状图/饼图/表格/地图/文本等 */
    private String componentType;
    /** 配置模板JSON */
    private String configTemplate;
    private String icon;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
