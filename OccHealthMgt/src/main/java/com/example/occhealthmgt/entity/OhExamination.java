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
@TableName("oh_examination")
public class OhExamination {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 员工姓名 */
    private String employeeName;
    /** 部门 */
    private String dept;
    /** 岗位 */
    private String post;
    /** 检查类型(上岗前/在岗期间/离岗时) */
    private String examType;
    /** 检查日期 */
    private LocalDate examDate;
    /** 检查机构 */
    private String hospital;
    /** 检查结论 */
    private String result;
    /** 危害因素 */
    private String hazardFactor;
    /** 下次检查日期 */
    private LocalDate nextExamDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
