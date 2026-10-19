package com.example.talentreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Talent Review Entity (人才评审)
 */
@Data
@TableName("talent_review")
public class TalentReview {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("employee_id")
    private Long employeeId;

    @TableField("employee_name")
    private String employeeName;

    @TableField("review_period")
    private String reviewPeriod;

    @TableField("reviewer")
    private String reviewer;

    @TableField("potential")
    private String potential;

    @TableField("performance")
    private String performance;

    @TableField("nine_box")
    private String nineBox;

    @TableField("summary")
    private String summary;

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
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
}
