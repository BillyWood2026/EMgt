package com.example.bimgt.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** BI数据集管理 */
@Data
@TableName("bi_dataset")
public class BiDataset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String datasetCode;
    private String datasetName;
    private Long datasourceId;
    /** SQL或模型定义 */
    private String modelDef;
    /** 字段说明JSON */
    private String fieldDesc;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
