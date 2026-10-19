package com.example.partyaffairsmgt.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 发展党员流程节点记录（5.2）。 */
@Data
@TableName("dev_node")
public class DevNode {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long personId;
    private String stage;
    private String nodeKey;
    private String nodeName;
    private LocalDate nodeDate;
    /** 差异化数据：谈话人/票数/学时/公示日期等 */
    private String payloadJson;
    /** 已交材料清单 JSON 数组 */
    private String materialsJson;
    private Integer needAudit;
    /** 已通过/待审批/已驳回 */
    private String auditStatus;
    private String approver;
    private String auditOpinion;
    private LocalDateTime auditTime;
    private String operator;
    private String remark;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
