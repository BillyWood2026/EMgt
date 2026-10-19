package com.example.occhealthmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("oh_health_record")
public class OhHealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 员工姓名 */
    private String employeeName;
    /** 部门 */
    private String dept;
    /** 岗位 */
    private String post;
    /** 性别 */
    private String gender;
    /** 年龄 */
    private Integer age;
    /** 接触危害因素 */
    private String hazardFactor;
    /** 检查次数 */
    private Integer examCount;
    /** 最近检查日期 */
    private LocalDate lastExamDate;
    /** 档案状态 */
    private String status;
    /** 备注 */
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
