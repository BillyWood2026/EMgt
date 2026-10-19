package com.example.partyaffairsmgt.opinion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 采集日志（需求 8.1：命中数、重试次数、失败告警）。 */
@Data
@TableName("opinion_collect_log")
public class OpinionCollectLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String taskName;
    private LocalDateTime collectTime;
    private Integer hitCount;
    /** 去重后新增数 */
    private Integer newCount;
    /** 重试次数（上限3） */
    private Integer retryCount;
    /** 成功/重试中/失败/中断告警 */
    private String status;
    private String errorMsg;
    private LocalDateTime createTime;
}
