package com.example.perf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.perf.entity.PerfArchive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 绩效结果归档 Mapper
 */
@Mapper
public interface PerfArchiveMapper extends BaseMapper<PerfArchive> {

    /**
     * 按 rank_no 排序查询（MySQL 8 中 rank 是保留字，不能用 Lambda 引用）。
     */
    @Select("SELECT id, employee_name, department, position, period, score, grade, rank_no AS `rank`, reviewer, comment, appeal_status, deleted, version, create_time, create_by, update_time, update_by FROM perf_archive WHERE deleted = 0 ORDER BY rank_no ASC")
    List<PerfArchive> listOrdered();

    /**
     * 按 id 查询详情。MP 默认 selectById 会生成 "rank_no AS rank"，别名 rank 是 MySQL8 保留字，
     * 未转义导致 SQL 语法错误；此处显式加反引号。
     */
    @Select("SELECT id, employee_name, department, position, period, score, grade, rank_no AS `rank`, reviewer, comment, appeal_status, deleted, version, create_time, create_by, update_time, update_by FROM perf_archive WHERE id = #{id} AND deleted = 0")
    PerfArchive selectDetailById(Long id);
}
