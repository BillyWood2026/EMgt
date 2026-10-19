package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("org_life_meeting")
public class OrgLifeMeeting {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    /** 民主生活会 / 组织生活会 */
    private String meetingType;
    private String title;
    private String topic;
    private LocalDate meetingDate;
    private String location;
    private String host;
    /** 参会党员 JSON 名单 */
    private String participants;
    /** 列席人员 JSON 名单 */
    private String attendeesNonvoting;
    private String absentee;
    /** 是否事后补录 */
    private Integer supplement;
    private String supplementReason;
    /** 议程/发言记录（富文本） */
    private String agendaContent;
    /** 四类会前材料是否齐备 */
    private Integer preMaterialsReady;
    private String minutes;
    /** 计划中/已召开/已归档 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
