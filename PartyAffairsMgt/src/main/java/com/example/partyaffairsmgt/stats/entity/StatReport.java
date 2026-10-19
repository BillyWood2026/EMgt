package com.example.partyaffairsmgt.stats.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("stat_report")
public class StatReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportNo;
    private String title;
    private String reportType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String content;
    private String generator;
    private LocalDateTime generateTime;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
