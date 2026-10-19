package com.example.partyaffairsmgt.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 三会一课专项台账（需求 9.4：四本台账归集、补录显著标记）。 */
@Data
@TableName("three_meetings_lesson")
public class ThreeMeetingsLesson {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    /** 支部党员大会/支部委员会/党小组会/党课 */
    private String meetingType;
    private String title;
    private LocalDate holdDate;
    private String location;
    private String host;
    private Integer shouldCount;
    private Integer actualCount;
    private String content;
    /** 已归档/待归档 */
    private String status;
    private Long planId;
    private Long recordId;
    private String recorder;
    private Integer signCount;
    /** 补录标记（台账中显著标记） */
    private Integer isBackfill;
    private String backfillReason;
    private String lecturer;
    private String lecturerRole;
    private LocalDateTime archiveTime;
    private String archivist;
    private Integer periodYear;
    private Integer periodQuarter;
    private Integer periodMonth;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
