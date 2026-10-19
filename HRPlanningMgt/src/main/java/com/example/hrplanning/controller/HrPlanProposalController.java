package com.example.hrplanning.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.HrPlanProposal;
import com.example.hrplanning.service.HrPlanProposalService;
import com.example.hrplanning.util.PageResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 人力规划提案 /api/hr-planning/proposal
 * 前端 hr-planning/Index.vue「规划提案」tab：GET /page 直接读取 {records,total}
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/hr-planning/proposal")
public class HrPlanProposalController {

    @Autowired
    private HrPlanProposalService hrPlanProposalService;

    @GetMapping("/page")
    public Map<String, Object> page(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "orgCode", required = false) String orgCode,
            @RequestParam(value = "status", required = false) String status) {
        IPage<HrPlanProposal> page = hrPlanProposalService.page(pageNum, pageSize, orgCode, status);
        return PageResponses.of(page);
    }

    @GetMapping("/list")
    public ResponseResult<java.util.List<HrPlanProposal>> list() {
        return ResponseResult.success(hrPlanProposalService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<HrPlanProposal> getById(@PathVariable Long id) {
        return ResponseResult.success(hrPlanProposalService.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody HrPlanProposal proposal) {
        hrPlanProposalService.save(proposal);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody HrPlanProposal proposal) {
        proposal.setId(id);
        hrPlanProposalService.updateById(proposal);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        hrPlanProposalService.removeById(id);
        return ResponseResult.success();
    }
}
