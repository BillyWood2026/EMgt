package com.example.perf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人才池 Entity（/api/talent-pool，perf_mgt.talent_pool）
 * poolType：SUCCESSION(继任池)/KEY_TALENT(关键人才)/HIGH_POTENTIAL(高潜池)
 * status：ACTIVE(在池)/FROZEN(冻结)/REMOVED(出池)
 */
@Data
@TableName("talent_pool")
public class TalentPool {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("employee_no")
    private String employeeNo;

    @TableField("employee_name")
    private String employeeName;

    @TableField("department")
    private String department;

    @TableField("position_name")
    private String positionName;

    /** 人才池类型 SUCCESSION/KEY_TALENT/HIGH_POTENTIAL */
    @TableField("pool_type")
    private String poolType;

    /** 九宫格定位 */
    @TableField("nine_box")
    private String nineBox;

    /** 就绪度 */
    @TableField("readiness")
    private String readiness;

    /** 匹配分 */
    @TableField("match_score")
    private BigDecimal matchScore;

    /** 状态 ACTIVE/FROZEN/REMOVED */
    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;

    @TableField("deleted")
    @TableLogic
    private Integer deleted = 0;

    @TableField("version")
    @Version
    private Integer version = 1;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;
}
