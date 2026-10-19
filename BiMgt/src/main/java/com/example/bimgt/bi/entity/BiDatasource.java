package com.example.bimgt.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** BI数据源管理 */
@Data
@TableName("bi_datasource")
public class BiDatasource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String datasourceName;
    /** 类型:MYSQL/POSTGRESQL/API */
    private String datasourceType;
    /** 连接配置JSON */
    private String connConfig;
    /** 状态:启用/停用 */
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
