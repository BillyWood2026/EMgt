package com.example.culturemgt.foundation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.culturemgt.foundation.entity.Redline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RedlineMapper extends BaseMapper<Redline> {

    /** 取当年最大编号（含逻辑删除行，避免 uk_redline_code 撞号）。 */
    @Select("SELECT MAX(code) FROM cul_redline WHERE code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxCode(@Param("prefix") String prefix);
}
