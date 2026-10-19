package com.example.orgmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orgmgt.entity.JobDescription;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface JobDescriptionMapper extends BaseMapper<JobDescription> {

    @Insert("INSERT INTO job_description (position_code, position_name, org_unit, position_level, headcount, " +
            "job_summary, main_responsibilities, work_permissions, education_requirement, major_requirement, " +
            "experience_requirement, skill_requirement, quality_requirement, work_relationship, kpi_indicators, " +
            "salary_range, duty_boundary, reviewer, abandon_time, change_note, remarks, version_no, status, write_status, effective_time, created_by, create_time, " +
            "updated_by, update_time, deleted, version) VALUES (#{positionCode}, #{positionName}, #{orgUnit}, " +
            "#{positionLevel}, #{headcount}, #{jobSummary}, #{mainResponsibilities}, #{workPermissions}, " +
            "#{educationRequirement}, #{majorRequirement}, #{experienceRequirement}, #{skillRequirement}, " +
            "#{qualityRequirement}, #{workRelationship}, #{kpiIndicators}, #{salaryRange}, #{dutyBoundary}, " +
            "#{reviewer}, #{abandonTime}, #{changeNote}, #{remarks}, " +
            "#{versionNo}, #{status}, #{writeStatus}, #{effectiveTime}, #{createdBy}, #{createTime}, " +
            "#{updatedBy}, #{updateTime}, 0, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(JobDescription jobDescription);

    @Update("UPDATE job_description SET position_code=#{positionCode}, position_name=#{positionName}, " +
            "org_unit=#{orgUnit}, position_level=#{positionLevel}, headcount=#{headcount}, " +
            "job_summary=#{jobSummary}, main_responsibilities=#{mainResponsibilities}, " +
            "work_permissions=#{workPermissions}, education_requirement=#{educationRequirement}, " +
            "major_requirement=#{majorRequirement}, experience_requirement=#{experienceRequirement}, " +
            "skill_requirement=#{skillRequirement}, quality_requirement=#{qualityRequirement}, " +
            "work_relationship=#{workRelationship}, kpi_indicators=#{kpiIndicators}, salary_range=#{salaryRange}, " +
            "duty_boundary=#{dutyBoundary}, reviewer=#{reviewer}, abandon_time=#{abandonTime}, change_note=#{changeNote}, " +
            "remarks=#{remarks}, version_no=#{versionNo}, status=#{status}, write_status=#{writeStatus}, " +
            "effective_time=#{effectiveTime}, updated_by=#{updatedBy}, update_time=#{updateTime}, " +
            "version=#{version} WHERE id=#{id}")
    int update(JobDescription jobDescription);

    @Update("UPDATE job_description SET deleted=1 WHERE id=#{id}")
    int deleteById(Long id);
    
    @Select("<script>" +
            "SELECT * FROM job_description WHERE deleted=0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (position_code LIKE CONCAT('%', #{keyword}, '%') OR position_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='orgUnit != null and orgUnit != \"\"'>" +
            "AND org_unit = #{orgUnit} " +
            "</if>" +
            "<if test='positionLevel != null and positionLevel != \"\"'>" +
            "AND position_level = #{positionLevel} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY create_time DESC " +
            "LIMIT #{pageSize} OFFSET #{offset}" +
            "</script>")
    List<JobDescription> selectList(@Param("keyword") String keyword, @Param("orgUnit") String orgUnit,
                                     @Param("positionLevel") String positionLevel, @Param("status") String status,
                                     @Param("page") Integer page, @Param("pageSize") Integer pageSize,
                                     @Param("offset") Integer offset);
    
    @Select("SELECT COUNT(*) FROM job_description WHERE deleted=0")
    int countAll();
    
    @Select("SELECT COUNT(*) FROM job_description WHERE status=#{status} AND deleted=0")
    int countByStatus(String status);
    
    @Select("SELECT * FROM job_description WHERE position_code=#{positionCode} AND deleted=0 ORDER BY create_time DESC LIMIT 1")
    JobDescription selectLatestVersion(String positionCode);
    
    @Select("SELECT * FROM job_description WHERE position_code=#{positionCode} AND deleted=0 ORDER BY create_time DESC")
    List<JobDescription> selectAllVersions(String positionCode);
    
    @Insert("INSERT INTO job_description_history (history_no, job_description_id, version_no, change_type, before_data, after_data, " +
            "initiator_name, change_desc, attachment_path, create_time) VALUES " +
            "(CONCAT('JDH', UNIX_TIMESTAMP(), FLOOR(RAND()*100000)), #{jobDescriptionId}, #{versionNo}, #{changeType}, " +
            "#{beforeData}, #{afterData}, #{initiator}, #{operationRecord}, #{attachmentPath}, NOW())")
    void insertHistory(@Param("jobDescriptionId") Long jobDescriptionId, @Param("versionNo") String versionNo, 
                       @Param("changeType") String changeType, @Param("beforeData") String beforeData, 
                       @Param("afterData") String afterData, @Param("initiator") String initiator, 
                       @Param("operationRecord") String operationRecord, @Param("attachmentPath") String attachmentPath);
    
    @Select("SELECT * FROM job_description_history WHERE job_description_id=#{jobDescriptionId} ORDER BY create_time DESC")
    List<Map<String, Object>> selectHistory(Long jobDescriptionId);
}
