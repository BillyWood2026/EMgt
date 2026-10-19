package com.example.strategymgt.dste.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dste_kpi")
public class DsteKpi {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String kpiCode;
    private String kpiName;
    private String kpiLevel;
    private String kpiDim;
    private String kpiDefinition;
    private String calcFormula;
    private String dataSource;
    private String statCycle;
    private BigDecimal targetValue;
    private BigDecimal warnThreshold;
    private BigDecimal redlineThreshold;
    private BigDecimal kpiWeight;
    private String primaryDept;
    private String dataProvider;
    private String status;
    private String versionNo;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
