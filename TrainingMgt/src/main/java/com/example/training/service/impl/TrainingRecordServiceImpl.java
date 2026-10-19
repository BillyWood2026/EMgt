package com.example.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.training.entity.TrainingRecord;
import com.example.training.mapper.TrainingRecordMapper;
import com.example.training.service.TrainingRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TrainingRecordServiceImpl extends ServiceImpl<TrainingRecordMapper, TrainingRecord>
        implements TrainingRecordService {

    @Override
    public IPage<TrainingRecord> pageRecords(long current, long size, String employeeName,
                                             String department, String courseName, String result) {
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(employeeName), TrainingRecord::getEmployeeName, employeeName)
                .eq(StringUtils.hasText(department), TrainingRecord::getDepartment, department)
                .like(StringUtils.hasText(courseName), TrainingRecord::getCourseName, courseName)
                .eq(StringUtils.hasText(result), TrainingRecord::getResult, result)
                .orderByDesc(TrainingRecord::getId);
        return this.page(new Page<>(current, size), wrapper);
    }
}
