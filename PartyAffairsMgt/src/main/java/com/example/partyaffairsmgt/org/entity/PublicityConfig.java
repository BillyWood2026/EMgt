package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 党务公开事项默认期限配置 */
@Data
@TableName("publicity_config")
public class PublicityConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String category;
    private String defaultScope;
    private String defaultMethods;
    private Integer durationDays;
    /** 期限是否按工作日计 */
    private Integer workdayFlag;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
