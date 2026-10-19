package com.example.partyaffairsmgt.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 组织岗位任职（需求 12.2）。 */
@Data
@TableName("sys_org_position")
public class SysOrgPosition {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    /** 书记/副书记/组织委员/宣传委员/纪检委员/委员 */
    private String positionName;
    private Long memberId;
    private String memberName;
    /** 是否兼职 0/1 */
    private Integer partTime;
    private LocalDate startDate;
    private LocalDate endDate;
    /** 在任/离任 */
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
