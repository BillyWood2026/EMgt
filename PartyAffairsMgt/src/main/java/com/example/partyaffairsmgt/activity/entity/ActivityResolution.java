package com.example.partyaffairsmgt.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 决议事项落实跟踪（需求 9.3：责任人+时限，到期提醒、超期预警联动考核）。 */
@Data
@TableName("activity_resolution")
public class ActivityResolution {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private String title;
    private String owner;
    private LocalDate deadline;
    /** 待落实/落实中/已落实 */
    private String status;
    private LocalDate finishTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
