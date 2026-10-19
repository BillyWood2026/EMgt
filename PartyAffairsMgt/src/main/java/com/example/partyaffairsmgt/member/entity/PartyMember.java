package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("party_member")
public class PartyMember {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String memberNo;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String idCard;
    private String ethnicity;
    private String education;
    private String phone;
    private Long orgId;
    private String orgName;
    private String position;
    private String employeeNo;
    private String nativePlace;
    private LocalDate joinDate;
    private LocalDate fullDate;
    private String memberStatus;
    /** 是否被停止表决权（留党察看等） */
    private Integer votingRightSuspended;
    private String address;
    /** 照片 */
    private String photoUrl;
    /** 政治面貌：正式党员/预备党员/共青团员/群众 */
    private String politicalStatus;
    /** 流动状态：在岗/流动/失联 */
    private String floatStatus;
    /** 档案材料是否完整 */
    private Integer archiveComplete;
    /** 联系方式修改审批状态 */
    private String contactAuditStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
