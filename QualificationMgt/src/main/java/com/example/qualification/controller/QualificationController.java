package com.example.qualification.controller;

import com.example.common.dto.ResponseResult;
import com.example.qualification.entity.QualificationStandard;
import com.example.qualification.service.QualificationStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 健康检查/兼容入口: GET /api/qualification/list 返回等级标准数组。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/qualification")
public class QualificationController {

    @Autowired
    private QualificationStandardService standardService;

    @GetMapping("/list")
    public ResponseResult<List<QualificationStandard>> list() {
        return ResponseResult.success(standardService.list());
    }
}
