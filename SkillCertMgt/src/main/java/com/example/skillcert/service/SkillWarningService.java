package com.example.skillcert.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillcert.entity.SkillWarning;

import java.util.Map;

public interface SkillWarningService extends IService<SkillWarning> {

    IPage<SkillWarning> pageWarnings(long current, long size, String warningType,
                                     String orgId, String skillTypeId, String keyword);

    boolean process(Long id, Map<String, Object> body);
}
