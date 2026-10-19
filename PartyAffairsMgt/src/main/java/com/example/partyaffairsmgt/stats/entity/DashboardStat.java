package com.example.partyaffairsmgt.stats.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dashboard_stat")
public class DashboardStat {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Integer orgCount;
    private Integer memberCount;
    private Integer fullMemberCount;
    private Integer probationCount;
    private Integer floatingMemberCount;
    private Integer activityCount;
    private Integer meetingCount;
    private BigDecimal dueCollected;
    private Integer petitionCount;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
