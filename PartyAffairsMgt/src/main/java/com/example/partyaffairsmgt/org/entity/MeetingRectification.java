package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 整改问题清单（跟踪销号） */
@Data
@TableName("meeting_rectification")
public class MeetingRectification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String problem;
    private String measure;
    private String ownerName;
    private LocalDate deadline;
    /** 未整改/整改中/已销号 */
    private String status;
    private String result;
    private String evidenceUrl;
    private LocalDate closeDate;
    /** 正常/临近到期/已超期/超期告警 */
    private String reminderLevel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
