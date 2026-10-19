package com.example.hrplanning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人才盘点(编制盘点) hr_talent_inventory
 */
@Data
@TableName("hr_talent_inventory")
public class TalentInventory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("employee_id")
    private String employeeId;

    @TableField("employee_name")
    private String employeeName;

    @TableField("org_level2")
    private String orgLevel2;

    @TableField("org_level3")
    private String orgLevel3;

    @TableField("position_sequence")
    private String positionSequence;

    @TableField("job_level")
    private String jobLevel;

    @TableField("headcount_type")
    private String headcountType;

    @TableField("age")
    private Integer age;

    @TableField("age_range")
    private String ageRange;

    @TableField("performance_level")
    private String performanceLevel;

    @TableField("turnover_risk")
    private String turnoverRisk;

    @TableField("is_core_talent")
    private Boolean isCoreTalent;

    @TableField("nine_box_position")
    private String nineBoxPosition;

    @TableField("headcount_status")
    private String headcountStatus;

    @TableField("snapshot_date")
    private LocalDate snapshotDate;

    /** 虚拟组织任职 */
    @TableField("virtual_org")
    private String virtualOrg;

    /** 司龄（如 3年2个月） */
    @TableField("company_age")
    private String companyAge;

    /** 是否临近退休 */
    @TableField("near_retirement")
    private Boolean nearRetirement;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;

    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;

    @Version
    @TableField("version")
    private Integer version = 1;
}
