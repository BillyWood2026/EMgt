package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 采集任务（需求 8.1：关键词组合 AND/OR、频率、配额、运行状态）。 */
@Data
@TableName("opinion_collect_task")
public class OpinionCollectTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 关键词组（逗号分隔） */
    private String keywordGroup;
    /** 信源（逗号分隔） */
    private String sources;
    /** 匹配模式 AND/OR */
    private String matchMode;
    /** 频率：每30分钟/每小时/每天 */
    private String frequency;
    /** 单次采集配额（超限限流） */
    private Integer quota;
    private Integer collectCount;
    private LocalDateTime lastCollectTime;
    /** 运行中/已暂停/异常 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
