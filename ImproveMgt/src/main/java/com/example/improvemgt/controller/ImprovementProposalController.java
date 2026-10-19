package com.example.improvemgt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.improvemgt.entity.ImprovementProposal;
import com.example.improvemgt.service.ImprovementProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/improve/proposal")
public class ImprovementProposalController {

    @Autowired
    private ImprovementProposalService proposalService;

    @GetMapping({"/list", ""})
    public ResponseResult<List<ImprovementProposal>> list() {
        return ResponseResult.success(proposalService.list());
    }

    @GetMapping("/page")
    public ResponseResult<IPage<ImprovementProposal>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String department) {
        Page<ImprovementProposal> page = new Page<>(current, size);
        return ResponseResult.success(proposalService.page(page, status, type, department));
    }

    @GetMapping("/{id}")
    public ResponseResult<ImprovementProposal> getById(@PathVariable Long id) {
        return ResponseResult.success(proposalService.getById(id));
    }

    @PostMapping
    public ResponseResult<ImprovementProposal> create(@RequestBody ImprovementProposal proposal) {
        return ResponseResult.success(proposalService.create(proposal));
    }

    @PutMapping("/{id}")
    public ResponseResult<ImprovementProposal> update(@PathVariable Long id, @RequestBody ImprovementProposal proposal) {
        proposal.setId(id);
        return ResponseResult.success(proposalService.update(proposal));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        proposalService.deleteById(id);
        return ResponseResult.success();
    }

    @DeleteMapping("/batch")
    public ResponseResult<Void> batchDelete(@RequestBody List<Long> ids) {
        proposalService.batchDelete(ids);
        return ResponseResult.success();
    }

    @PostMapping("/{id}/review")
    public ResponseResult<ImprovementProposal> review(@PathVariable Long id,
                                                      @RequestBody(required = false) java.util.Map<String, Object> body) {
        boolean approved = true;
        String comment = null;
        if (body != null) {
            Object a = body.get("approved");
            if (a != null) {
                approved = Boolean.parseBoolean(String.valueOf(a));
            }
            Object c = body.get("comment");
            if (c != null) {
                comment = String.valueOf(c);
            }
        }
        return ResponseResult.success(proposalService.review(id, approved, comment));
    }

    @PostMapping("/{id}/start-implementation")
    public ResponseResult<ImprovementProposal> startImplementation(@PathVariable Long id) {
        return ResponseResult.success(proposalService.markImplementing(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseResult<ImprovementProposal> complete(@PathVariable Long id) {
        return ResponseResult.success(proposalService.markCompleted(id));
    }
}
