package com.example.skillcert.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillcert.entity.SkillCertificate;

import java.util.Map;

public interface SkillCertificateService extends IService<SkillCertificate> {

    Map<String, Object> pageList(long current, long size, String employeeName, String skillName,
                                  String status, String certStatus, String certType,
                                  String certLevel, String keyword);

    boolean revoke(Long id);

    boolean approve(Long id);

    boolean reject(Long id);
}
