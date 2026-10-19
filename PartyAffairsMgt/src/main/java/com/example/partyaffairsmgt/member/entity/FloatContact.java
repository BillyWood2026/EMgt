package com.example.partyaffairsmgt.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("float_contact")
public class FloatContact {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long floatingId;
    private Long memberId;
    private LocalDate contactDate;
    private String contactWay;
    private String content;
    private String recorder;
    private LocalDateTime createTime;
}
