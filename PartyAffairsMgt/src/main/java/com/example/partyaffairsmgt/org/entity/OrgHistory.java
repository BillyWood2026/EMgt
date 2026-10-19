package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 组织历史沿革 */
@Data
@TableName("org_history")
public class OrgHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    /** 设立/更名/隶属调整/合并/撤销/换届/批复 */
    private String changeType;
    private String fromValue;
    private String toValue;
    private LocalDate changeDate;
    private String approvalNo;
    private String operator;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
