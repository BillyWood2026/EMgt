package com.example.partyaffairsmgt.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.partyaffairsmgt.system.entity.SysDictItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {

    /** 物理删除（仅在未被业务引用时调用，E-SY-10）。 */
    @Delete("DELETE FROM sys_dict_item WHERE id = #{id}")
    int physicalDelete(@Param("id") Long id);
}
