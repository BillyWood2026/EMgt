package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 合并/撤销记录与影响分析结果 */
@Data
@TableName("org_merge_record")
public class OrgMergeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** MERGE 合并 / REVOKE 撤销 */
    private String bizType;
    private Long sourceOrgId;
    private String sourceOrgName;
    private String sourceCode;
    private Long targetOrgId;
    private String targetOrgName;
    private Integer affectedMembers;
    private Integer childOrgCount;
    private Integer pendingFlows;
    private Integer unpaidDues;
    private String reason;
    /** DRAFT/EXECUTED/REJECTED */
    private String status;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
