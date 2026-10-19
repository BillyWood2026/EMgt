package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("care_difficulty")
public class CareDifficulty {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private Long orgId;
    private String orgName;
    private String difficultyType;
    private String difficultyDesc;
    private LocalDate applyDate;
    private LocalDate approveDate;
    private String approver;
    private String status;
    private LocalDate exitDate;
    private String exitReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
