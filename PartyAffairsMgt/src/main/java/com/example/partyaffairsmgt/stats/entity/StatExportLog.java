package com.example.partyaffairsmgt.stats.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 导出审计日志（需求 11.3）。 */
@Data
@TableName("stat_export_log")
public class StatExportLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportKey;
    private String reportName;
    private String format;
    private Integer rowCount;
    /** SYNC/ASYNC */
    private String exportMode;
    private String maskedFields;
    private String exportBy;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
