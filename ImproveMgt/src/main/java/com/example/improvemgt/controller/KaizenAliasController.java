package com.example.improvemgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.improvemgt.entity.ImprovementImplementation;
import com.example.improvemgt.entity.ImprovementProposal;
import com.example.improvemgt.entity.ImprovementResult;
import com.example.improvemgt.service.ImprovementImplementationService;
import com.example.improvemgt.service.ImprovementProposalService;
import com.example.improvemgt.service.ImprovementResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * /api/kaizen 别名（vite 代理 /api/kaizen -> 8142），与 /api/improve 共用服务
 */
@RestController
@RequestMapping("/api/kaizen")
public class KaizenAliasController {

    @Autowired
    private ImprovementProposalService proposalService;
    @Autowired
    private ImprovementImplementationService implementationService;
    @Autowired
    private ImprovementResultService resultService;

    // ---------------- 提案 ----------------

    @GetMapping({"/proposal/list", "/proposal"})
    public ResponseResult<List<ImprovementProposal>> proposalList() {
        return ResponseResult.success(proposalService.list());
    }

    @GetMapping("/proposal/page")
    public ResponseResult<?> proposalPage(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) String department) {
        return ResponseResult.success(proposalService.page(new Page<>(current, size), status, type, department));
    }

    @GetMapping("/proposal/{id}")
    public ResponseResult<ImprovementProposal> proposalGet(@PathVariable Long id) {
        return ResponseResult.success(proposalService.getById(id));
    }

    @PostMapping("/proposal")
    public ResponseResult<ImprovementProposal> proposalCreate(@RequestBody ImprovementProposal proposal) {
        return ResponseResult.success(proposalService.create(proposal));
    }

    @PutMapping("/proposal/{id}")
    public ResponseResult<ImprovementProposal> proposalUpdate(@PathVariable Long id,
                                                              @RequestBody ImprovementProposal proposal) {
        proposal.setId(id);
        return ResponseResult.success(proposalService.update(proposal));
    }

    @DeleteMapping("/proposal/{id}")
    public ResponseResult<Void> proposalDelete(@PathVariable Long id) {
        proposalService.deleteById(id);
        return ResponseResult.success();
    }

    @PostMapping("/proposal/{id}/review")
    public ResponseResult<ImprovementProposal> proposalReview(@PathVariable Long id,
                                                              @RequestBody(required = false) Map<String, Object> body) {
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

    // ---------------- 实施 ----------------

    @GetMapping({"/implementation/list", "/implementation"})
    public ResponseResult<List<ImprovementImplementation>> implList() {
        return ResponseResult.success(implementationService.list());
    }

    @GetMapping("/implementation/page")
    public ResponseResult<?> implPage(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String department) {
        return ResponseResult.success(implementationService.page(new Page<>(current, size), status, department));
    }

    @GetMapping("/implementation/{id}")
    public ResponseResult<ImprovementImplementation> implGet(@PathVariable Long id) {
        return ResponseResult.success(implementationService.getById(id));
    }

    @PostMapping("/implementation")
    public ResponseResult<ImprovementImplementation> implCreate(@RequestBody ImprovementImplementation impl) {
        return ResponseResult.success(implementationService.create(impl));
    }

    @PutMapping("/implementation/{id}")
    public ResponseResult<ImprovementImplementation> implUpdate(@PathVariable Long id,
                                                                @RequestBody ImprovementImplementation impl) {
        impl.setId(id);
        return ResponseResult.success(implementationService.update(impl));
    }

    @DeleteMapping("/implementation/{id}")
    public ResponseResult<Void> implDelete(@PathVariable Long id) {
        implementationService.deleteById(id);
        return ResponseResult.success();
    }

    @PostMapping("/implementation/{id}/{action}")
    public ResponseResult<ImprovementImplementation> implAction(@PathVariable Long id,
                                                                @PathVariable String action) {
        return switch (action) {
            case "start" -> ResponseResult.success(implementationService.start(id));
            case "pause" -> ResponseResult.success(implementationService.pause(id));
            case "resume" -> ResponseResult.success(implementationService.resume(id));
            case "complete" -> ResponseResult.success(implementationService.complete(id));
            default -> throw new IllegalArgumentException("不支持的实施动作: " + action);
        };
    }

    // ---------------- 成果 ----------------

    @GetMapping({"/result/list", "/result"})
    public ResponseResult<List<ImprovementResult>> resultList() {
        return ResponseResult.success(resultService.list());
    }

    @GetMapping("/result/page")
    public ResponseResult<?> resultPage(@RequestParam(defaultValue = "1") Integer current,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String type) {
        return ResponseResult.success(resultService.page(new Page<>(current, size), status, type));
    }

    @GetMapping("/result/{id}")
    public ResponseResult<ImprovementResult> resultGet(@PathVariable Long id) {
        return ResponseResult.success(resultService.getById(id));
    }

    @PostMapping("/result")
    public ResponseResult<ImprovementResult> resultCreate(@RequestBody ImprovementResult result) {
        return ResponseResult.success(resultService.create(result));
    }

    @PutMapping("/result/{id}")
    public ResponseResult<ImprovementResult> resultUpdate(@PathVariable Long id,
                                                          @RequestBody ImprovementResult result) {
        result.setId(id);
        return ResponseResult.success(resultService.update(result));
    }

    @DeleteMapping("/result/{id}")
    public ResponseResult<Void> resultDelete(@PathVariable Long id) {
        resultService.deleteById(id);
        return ResponseResult.success();
    }

    @PostMapping("/result/{id}/confirm")
    public ResponseResult<ImprovementResult> resultConfirm(@PathVariable Long id) {
        return ResponseResult.success(resultService.confirm(id));
    }

    @PostMapping("/result/{id}/award")
    public ResponseResult<ImprovementResult> resultAward(@PathVariable Long id,
                                                         @RequestBody(required = false) Map<String, Object> body) {
        BigDecimal amount = null;
        if (body != null && body.get("rewardAmount") != null) {
            amount = new BigDecimal(String.valueOf(body.get("rewardAmount")));
        }
        return ResponseResult.success(resultService.award(id, amount));
    }
}
