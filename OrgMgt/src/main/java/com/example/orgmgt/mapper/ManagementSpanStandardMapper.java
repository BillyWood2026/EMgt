package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.ManagementSpanStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ManagementSpanStandardMapper extends BaseMapper<ManagementSpanStandard> {
    
    @Select("SELECT * FROM management_span_standard WHERE position_level=#{positionLevel} AND deleted=0")
    ManagementSpanStandard selectByPositionLevel(@Param("positionLevel") String positionLevel);
    
    @Select("SELECT * FROM management_span_standard WHERE deleted=0")
    List<ManagementSpanStandard> selectActiveStandards();
    
    @Select("SELECT * FROM management_span_standard WHERE deleted=0 ORDER BY position_level")
    List<ManagementSpanStandard> selectAllLevels();
    
    @Select("SELECT CASE " +
            "WHEN #{currentSpan} &gt; max_span THEN 'EXCEEDED' " +
            "WHEN #{currentSpan} &gt;= standard_span THEN 'WARNING' " +
            "ELSE 'NORMAL' END " +
            "FROM management_span_standard WHERE position_level=#{positionLevel} AND deleted=0")
    String validateSpan(@Param("positionLevel") String positionLevel, @Param("currentSpan") Integer currentSpan);
}
