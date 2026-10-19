package com.example.culturemgt.training.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.training.entity.Cert;
import com.example.culturemgt.training.service.ArchiveService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 培训档案与认证（EL-CUL-009 员工文化培训档案抽屉）。 */
@RestController
@RequestMapping("/api/culture/archive")
public class ArchiveController {

    @Autowired private ArchiveService service;

    /** 全员档案聚合（?frozen=1 资格冻结名单）。 */
    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Integer frozen) {
        return ResponseResult.success(service.list(keyword, frozen));
    }

    /** 个人档案详情：学习记录/考试成绩/补考记录/证书/合规状态。 */
    @GetMapping("/{userName}")
    public ResponseResult<?> detail(@PathVariable String userName) {
        return ResponseResult.success(service.detail(userName));
    }

    /** 手工颁发认证证书，body {userName, certType, reason}。 */
    @PostMapping("/cert")
    public ResponseResult<?> issueCert(@RequestBody Map<String, String> body) {
        return ResponseResult.success(service.issueCert(
                body.get("userName"), body.get("certType"), body.get("reason")));
    }
}
