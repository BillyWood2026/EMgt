package com.example.culturemgt.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cul_activity_sign")
public class ActivitySign {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private String userName;
    private String userDept;
    private LocalDateTime signTime;
    private String signStatus;
    private String checkStatus;
    private LocalDateTime checkTime;
    private String checkBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
