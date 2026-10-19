package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("party_org")
public class PartyOrg {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orgNo;
    /** 层级编码，如 01.02.03，自动生成不可修改 */
    private String orgCode;
    /** 设立批复文号 */
    private String approvalNo;
    private String name;
    private String shortName;
    /** 党组/党委/党总支/党支部/党小组/临时党支部 */
    private String orgType;
    private Long parentId;
    private String secretary;
    private String deputySecretary;
    /** 委员及职务（文本，形如 "组织委员:张三"） */
    private String committeeMembers;
    /** 当前届次 */
    private Integer termNo;
    private LocalDate termStart;
    private LocalDate termEnd;
    /** 是否流动党员党组织 */
    private Integer isFloating;
    /** 党小组划分 JSON：[{name,leader}] */
    private String partyGroups;
    private String remark;
    /** 待核实标记（E-ORG-02/05 预警） */
    private Integer verifyFlag;
    /** PENDING 待审核 / APPROVED 已批复 */
    private String auditStatus;
    private String contactPhone;
    private String address;
    private LocalDate establishedDate;
    private Integer sortOrder;
    /** 筹备/正常/换届中/合并/撤销 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
