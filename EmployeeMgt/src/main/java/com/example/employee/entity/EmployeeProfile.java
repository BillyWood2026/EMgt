package com.example.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("employee_profile")
public class EmployeeProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("employee_id")
    private Long employeeId;

    @TableField("education")
    private String education;

    @TableField("school")
    private String school;

    @TableField("major")
    private String major;

    @TableField("graduation_date")
    private LocalDate graduationDate;

    @TableField("nation")
    private String nation;

    @TableField("political_status")
    private String politicalStatus;

    @TableField("former_name")
    private String formerName;

    @TableField("marital_status")
    private String maritalStatus;

    @TableField("hukou_address")
    private String hukouAddress;

    @TableField("hukou_type")
    private String hukouType;

    @TableField("degree")
    private String degree;

    @TableField("foreign_lang_level")
    private String foreignLangLevel;

    @TableField("computer_level")
    private String computerLevel;

    @TableField("rank_level")
    private String rankLevel;

    @TableField("salary_grade")
    private String salaryGrade;

    @TableField("job_family")
    private String jobFamily;

    @TableField("position_seq")
    private String positionSeq;

    @TableField("on_job_status")
    private String onJobStatus;

    @TableField("archive_status")
    private String archiveStatus;

    @TableField("work_years")
    private Integer workYears;

    @TableField("previous_company")
    private String previousCompany;

    @TableField("skills")
    private String skills;

    @TableField("certifications")
    private String certifications;

    @TableField("address")
    private String address;

    @TableField("emergency_contact")
    private String emergencyContact;

    @TableField("emergency_phone")
    private String emergencyPhone;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;
}
