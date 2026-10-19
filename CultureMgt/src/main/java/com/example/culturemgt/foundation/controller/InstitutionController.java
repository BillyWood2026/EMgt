package com.example.culturemgt.foundation.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.foundation.entity.Institution;
import com.example.culturemgt.foundation.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/culture/foundation/institution")
public class InstitutionController {

    @Autowired private InstitutionService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) String docType,
                                  @RequestParam(required = false) String docName) {
        return ResponseResult.success(service.list(status, docType, docName));
    }

    @GetMapping("/{id}")
    public ResponseResult<?> get(@PathVariable Long id) {
        return ResponseResult.success(service.get(id));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody Institution d) {
        d.setId(null);
        return ResponseResult.success(service.save(d));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody Institution d) {
        d.setId(id);
        return ResponseResult.success(service.save(d));
    }

    @PostMapping("/{id}/submit")
    public ResponseResult<?> submit(@PathVariable Long id) {
        service.submit(id);
        return ResponseResult.success(true);
    }

    @PostMapping("/{id}/approve")
    public ResponseResult<?> approve(@PathVariable Long id,
                                     @RequestParam boolean pass,
                                     @RequestParam(required = false) String opinion) {
        service.approve(id, pass, opinion);
        return ResponseResult.success(true);
    }

    @PostMapping("/{id}/revise")
    public ResponseResult<?> revise(@PathVariable Long id, @RequestParam String reason) {
        return ResponseResult.success(service.revise(id, reason));
    }

    @PostMapping("/{id}/learn")
    public ResponseResult<?> learn(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String userName = body == null ? null : body.get("userName");
        String orgName = body == null ? null : body.get("orgName");
        service.learn(id, userName, orgName);
        return ResponseResult.success(true);
    }

    @GetMapping("/{id}/learners")
    public ResponseResult<?> learners(@PathVariable Long id) {
        return ResponseResult.success(service.learners(id));
    }

    @GetMapping("/{id}/compliance")
    public ResponseResult<?> compliance(@PathVariable Long id) {
        return ResponseResult.success(Map.of("rate", service.complianceRate(id)));
    }
}
