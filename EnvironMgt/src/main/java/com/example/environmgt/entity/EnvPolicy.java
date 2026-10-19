package com.example.environmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("env_policy")
public class EnvPolicy {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 制度标题 */
    private String title;
    /** 文号 */
    private String policyNo;
    /** 发布机构 */
    private String publishOrg;
    /** 发布日期 */
    private LocalDate publishDate;
    /** 生效日期 */
    private LocalDate effectiveDate;
    /** 正文 */
    private String content;
    /** 状态 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
