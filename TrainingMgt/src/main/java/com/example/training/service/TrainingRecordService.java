package com.example.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.training.entity.TrainingRecord;

public interface TrainingRecordService extends IService<TrainingRecord> {

    IPage<TrainingRecord> pageRecords(long current, long size, String employeeName,
                                      String department, String courseName, String result);
}
