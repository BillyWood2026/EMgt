package com.example.partyaffairsmgt.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("party_affair_publicity")
public class PartyAffairPublicity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String orgName;
    private String title;
    /** 党费收缴使用/民主评议/评优表彰/发展党员/组织设置等 */
    private String category;
    private String content;
    /** 全体党员/全体职工/社会公众 */
    private String publicScope;
    /** 公开方式 JSON 多选：系统公示栏/门户/公众号/会议通报 */
    private String methods;
    private String publisher;
    private String approver;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    /** 自动下架/归档/续期 */
    private String expireAction;
    private Integer published;
    private LocalDate publishDate;
    private String attachments;
    /** 草稿/待审核/已发布/已下架/已归档 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
