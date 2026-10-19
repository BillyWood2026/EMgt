package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.OperationLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    @Insert("INSERT INTO operation_log (log_no, business_module, business_id, business_code, business_name, " +
            "operation_type, operation_content, operator_id, operator_name, operator_org_id, operator_org_code, " +
            "operator_ip, before_data, after_data, change_summary, attachment_path, result_status, result_message, " +
            "operation_time, create_time) VALUES (#{logNo}, #{businessModule}, #{businessId}, #{businessCode}, " +
            "#{businessName}, #{operationType}, #{operationContent}, #{operatorId}, #{operatorName}, " +
            "#{operatorOrgId}, #{operatorOrgCode}, #{operatorIp}, #{beforeData}, #{afterData}, #{changeSummary}, " +
            "#{attachmentPath}, #{resultStatus}, #{resultMessage}, #{operationTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OperationLog log);
    
    @Select("SELECT * FROM operation_log WHERE business_module=#{businessModule} ORDER BY operation_time DESC")
    List<OperationLog> findByModule(String businessModule);
    
    @Select("SELECT * FROM operation_log WHERE business_module=#{businessModule} AND business_id=#{businessId} ORDER BY operation_time DESC")
    List<OperationLog> findByBusinessId(@Param("businessModule") String businessModule, @Param("businessId") Long businessId);
    
    @Select("SELECT * FROM operation_log WHERE operator_id=#{operatorId} ORDER BY operation_time DESC")
    List<OperationLog> findByOperatorId(String operatorId);
    
    @Select("SELECT * FROM operation_log WHERE operation_time BETWEEN #{startTime} AND #{endTime} ORDER BY operation_time DESC")
    List<OperationLog> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Select("SELECT * FROM operation_log WHERE operation_type=#{operationType} ORDER BY operation_time DESC")
    List<OperationLog> findByOperationType(String operationType);
    
    @Select("SELECT * FROM operation_log WHERE operation_content LIKE CONCAT('%', #{keyword}, '%') OR business_name LIKE CONCAT('%', #{keyword}, '%') ORDER BY operation_time DESC")
    List<OperationLog> searchByKeyword(String keyword);
    
    @Select("<script>" +
            "SELECT * FROM operation_log WHERE 1=1 " +
            "<if test='businessModule != null and businessModule != \"\"'>" +
            "AND business_module = #{businessModule} " +
            "</if>" +
            "<if test='operationType != null and operationType != \"\"'>" +
            "AND operation_type = #{operationType} " +
            "</if>" +
            "<if test='operatorId != null and operatorId != \"\"'>" +
            "AND operator_id = #{operatorId} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND operation_time &gt;= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND operation_time &lt;= #{endTime} " +
            "</if>" +
            "ORDER BY operation_time DESC " +
            "LIMIT #{pageSize} OFFSET #{offset}" +
            "</script>")
    List<OperationLog> findWithPagination(@Param("businessModule") String businessModule, 
                                          @Param("operationType") String operationType, 
                                          @Param("operatorId") String operatorId,
                                          @Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime,
                                          @Param("page") Integer page,
                                          @Param("pageSize") Integer pageSize,
                                          @Param("offset") Integer offset);
    
    @Select("<script>" +
            "SELECT COUNT(*) FROM operation_log WHERE 1=1 " +
            "<if test='businessModule != null and businessModule != \"\"'>" +
            "AND business_module = #{businessModule} " +
            "</if>" +
            "<if test='operationType != null and operationType != \"\"'>" +
            "AND operation_type = #{operationType} " +
            "</if>" +
            "<if test='operatorId != null and operatorId != \"\"'>" +
            "AND operator_id = #{operatorId} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND operation_time &gt;= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND operation_time &lt;= #{endTime} " +
            "</if>" +
            "</script>")
    int countByConditions(@Param("businessModule") String businessModule, 
                          @Param("operationType") String operationType, 
                          @Param("operatorId") String operatorId,
                          @Param("startTime") LocalDateTime startTime, 
                          @Param("endTime") LocalDateTime endTime);
    
    @Select("SELECT COUNT(*) FROM operation_log WHERE business_module=#{businessModule}")
    int countByModule(String businessModule);
    
    @Select("SELECT COUNT(*) FROM operation_log WHERE operator_id=#{operatorId}")
    int countByOperator(String operatorId);
    
    @Select("SELECT business_module, COUNT(*) as count FROM operation_log GROUP BY business_module ORDER BY count DESC")
    List<OperationLog> getStatisticsByModule();
    
    @Select("SELECT operation_type, COUNT(*) as count FROM operation_log GROUP BY operation_type ORDER BY count DESC")
    List<OperationLog> getStatisticsByOperationType();
}
