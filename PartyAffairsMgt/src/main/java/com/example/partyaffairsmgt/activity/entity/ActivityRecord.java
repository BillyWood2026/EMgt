package com.example.partyaffairsmgt.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 活动记录（需求 9.3：模板化会议记录、完整性校验、补录、与签到联动）。 */
@Data
@TableName("activity_record")
public class ActivityRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long orgId;
    private String orgName;
    private String title;
    private String activityType;
    private LocalDate holdDate;
    private String location;
    private String host;
    /** 实到人数 */
    private Integer attendCount;
    private String content;
    private String minutes;
    private String attachments;
    private LocalDateTime archiveTime;
    private String archivist;
    /** 编辑中/待归档/已归档 */
    private String status;
    private String recorder;
    /** 应到人数 */
    private Integer expectCount;
    /** 签到人数（含补签） */
    private Integer signCount;
    private String agenda;
    private String speeches;
    private Integer inLedger;
    /** 是否补录（追记） */
    private Integer isBackfill;
    private String backfillReason;
    private String lecturer;
    private String lecturerRole;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
