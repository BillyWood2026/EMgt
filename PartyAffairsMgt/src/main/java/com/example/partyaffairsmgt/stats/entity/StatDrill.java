package com.example.partyaffairsmgt.stats.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("stat_drill")
public class StatDrill {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String dimension;
    private String metric;
    private String drillPath;
    private String configJson;
    private String description;
    private LocalDateTime lastUpdateTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
