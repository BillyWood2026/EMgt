package com.example.skillcert.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillcert.entity.SkillApplication;

import java.util.Map;

public interface SkillApplicationService extends IService<SkillApplication> {

    IPage<SkillApplication> pageApply(long current, long size);

    IPage<SkillApplication> pageReview(long current, long size, String reviewStatus, Long planId);

    boolean submitApplication(SkillApplication entity);

    boolean retake(Long id);

    boolean score(Map<String, Object> body);
}
