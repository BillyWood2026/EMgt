package com.example.salary.entity;

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
 * 矩阵包 / 岗位薪资等级 Entity（对应前端 Structure.vue）
 */
@Data
@TableName("salary_matrix_package")
public class SalaryMatrixPackage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 岗位等级，例如 L1 */
    @TableField("level")
    private String level;

    /** 等级名称，例如 初级 */
    @TableField("level_name")
    private String levelName;

    /** 基本工资 */
    @TableField("basic_salary")
    private BigDecimal basicSalary;

    /** 绩效工资 */
    @TableField("performance_salary")
    private BigDecimal performanceSalary;

    /** 合计 */
    @TableField("total_salary")
    private BigDecimal totalSalary;

    /** 岗位序列：管理序列/技术研发序列/职能支撑序列/业务营销序列/生产操作序列 */
    @TableField("job_sequence")
    private String jobSequence;

    /** 薪酬带宽下限 */
    @TableField("band_min")
    private BigDecimal bandMin;

    /** 薪酬带宽中位 */
    @TableField("band_mid")
    private BigDecimal bandMid;

    /** 薪酬带宽上限 */
    @TableField("band_max")
    private BigDecimal bandMax;

    /** 薪酬包类型：经营薪酬包/战略薪酬包 */
    @TableField("package_type")
    private String packageType;

    /** E/R值阈值(%) */
    @TableField("er_threshold")
    private BigDecimal erThreshold;

    /** 适用组织 */
    @TableField("apply_org")
    private String applyOrg;

    /** 适用岗位说明 */
    @TableField("description")
    private String description;

    /** 状态：生效 / 已失效 */
    @TableField("status")
    private String status;

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
