package com.example.auditmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditmgt.common.ResponseResult;
import com.example.auditmgt.entity.AuditFinding;
import com.example.auditmgt.entity.AuditProject;
import com.example.auditmgt.mapper.AuditFindingMapper;
import com.example.auditmgt.mapper.AuditProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit/finding")
@CrossOrigin(origins = "*")
public class AuditFindingController {

    /** 视为“已闭环/已整改”的终态（resolved 为历史兼容状态） */
    private static final List<String> CLOSED_STATES = Arrays.asList("closed", "resolved");

    @Autowired
    private AuditFindingMapper findingMapper;

    @Autowired
    private AuditProjectMapper projectMapper;

    @GetMapping("/list")
    public ResponseResult<Page<AuditFinding>> list(@RequestParam(required = false) String title,
                                                   @RequestParam(required = false) String level,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) Long projectId,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<AuditFinding> qw = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            qw.like(AuditFinding::getTitle, title);
        }
        if (level != null && !level.isEmpty()) {
            qw.eq(AuditFinding::getLevel, level);
        }
        if (status != null && !status.isEmpty()) {
            qw.eq(AuditFinding::getStatus, status);
        }
        if (projectId != null) {
            qw.eq(AuditFinding::getProjectId, projectId);
        }
        qw.orderByDesc(AuditFinding::getId);
        return ResponseResult.success(findingMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/statistics")
    public ResponseResult<Map<String, Object>> statistics() {
        List<AuditFinding> all = findingMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, Object> stat = new HashMap<>();
        long high = 0, medium = 0, low = 0;
        long pending = 0, correcting = 0, reviewing = 0, closed = 0, overdue = 0;
        LocalDate today = LocalDate.now();
        for (AuditFinding f : all) {
            String lvl = f.getLevel();
            if ("high".equals(lvl)) high++;
            else if ("medium".equals(lvl)) medium++;
            else if ("low".equals(lvl)) low++;

            String st = f.getStatus();
            boolean isClosed = st != null && CLOSED_STATES.contains(st);
            if (isClosed) {
                closed++;
            } else {
                if ("pending".equals(st)) pending++;
                else if ("correcting".equals(st)) correcting++;
                else if ("reviewing".equals(st)) reviewing++;
                if (f.getDeadline() != null && f.getDeadline().isBefore(today)) {
                    overdue++;
                }
            }
        }
        stat.put("total", all.size());
        stat.put("highCount", high);
        stat.put("mediumCount", medium);
        stat.put("lowCount", low);
        stat.put("pendingCount", pending);
        stat.put("correctingCount", correcting);
        stat.put("reviewingCount", reviewing);
        stat.put("closedCount", closed);
        // 兼容前端旧字段名
        stat.put("resolvedCount", closed);
        // 未整改/未闭环 = 总数 - 已闭环；超期 = 已过期限且未闭环
        stat.put("openCount", all.size() - closed);
        stat.put("overdueCount", overdue);
        return ResponseResult.success(stat);
    }

    @GetMapping("/{id}")
    public ResponseResult<AuditFinding> getById(@PathVariable Long id) {
        return ResponseResult.success(findingMapper.selectById(id));
    }

    private void fillProjectName(AuditFinding finding) {
        if (finding.getProjectId() != null && (finding.getProjectName() == null || finding.getProjectName().isEmpty())) {
            AuditProject project = projectMapper.selectById(finding.getProjectId());
            if (project != null) {
                finding.setProjectName(project.getName());
            }
        }
    }

    @PostMapping
    public ResponseResult<AuditFinding> create(@RequestBody AuditFinding finding) {
        if (finding.getStatus() == null || finding.getStatus().isEmpty()) {
            finding.setStatus("pending");
        }
        if (finding.getLevel() == null || finding.getLevel().isEmpty()) {
            finding.setLevel("medium");
        }
        fillProjectName(finding);
        finding.setId(null);
        finding.setDeleted(null);
        finding.setCreateTime(LocalDateTime.now());
        finding.setUpdateTime(LocalDateTime.now());
        findingMapper.insert(finding);
        return ResponseResult.success(finding);
    }

    @PutMapping("/{id}")
    public ResponseResult<AuditFinding> update(@PathVariable Long id, @RequestBody AuditFinding finding) {
        finding.setId(id);
        fillProjectName(finding);
        finding.setUpdateTime(LocalDateTime.now());
        findingMapper.updateById(finding);
        return ResponseResult.success(findingMapper.selectById(id));
    }

    /** 下发整改：待整改 -> 整改中，明确责任人与整改期限 */
    @PutMapping("/{id}/distribute")
    public ResponseResult<AuditFinding> distribute(@PathVariable Long id, @RequestBody(required = false) AuditFinding body) {
        AuditFinding finding = findingMapper.selectById(id);
        if (finding == null) {
            return ResponseResult.error("审计发现不存在");
        }
        if (!"pending".equals(finding.getStatus())) {
            return ResponseResult.error("非法状态流转：仅待整改(pending)可下发整改，当前[" + finding.getStatus() + "]");
        }
        if (body != null) {
            if (body.getResponsible() != null) finding.setResponsible(body.getResponsible());
            if (body.getDeadline() != null) finding.setDeadline(body.getDeadline());
            if (body.getRecommendation() != null) finding.setRecommendation(body.getRecommendation());
        }
        finding.setStatus("correcting");
        finding.setUpdateTime(LocalDateTime.now());
        findingMapper.updateById(finding);
        return ResponseResult.success(finding);
    }

    /** 提交整改：整改中 -> 待复核，必须填写整改措施 */
    @PutMapping("/{id}/submit-rectification")
    public ResponseResult<AuditFinding> submitRectification(@PathVariable Long id, @RequestBody AuditFinding body) {
        AuditFinding finding = findingMapper.selectById(id);
        if (finding == null) {
            return ResponseResult.error("审计发现不存在");
        }
        if (!"correcting".equals(finding.getStatus())) {
            return ResponseResult.error("非法状态流转：仅整改中(correcting)可提交整改，当前[" + finding.getStatus() + "]");
        }
        String measure = body == null ? null : body.getRectificationMeasure();
        if (measure == null || measure.trim().isEmpty()) {
            return ResponseResult.error("提交整改必须填写整改措施");
        }
        finding.setRectificationMeasure(measure);
        if (body.getResponsible() != null) finding.setResponsible(body.getResponsible());
        if (body.getDeadline() != null) finding.setDeadline(body.getDeadline());
        finding.setStatus("reviewing");
        finding.setRectifyTime(LocalDateTime.now());
        finding.setUpdateTime(LocalDateTime.now());
        findingMapper.updateById(finding);
        return ResponseResult.success(finding);
    }

    /**
     * 复核验证：待复核 -> 已关闭(pass) 或退回整改(!pass)。
     * body.passed 必填；pass 时记录复核人/意见/关闭时间。
     */
    @PutMapping("/{id}/review")
    public ResponseResult<AuditFinding> review(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        AuditFinding finding = findingMapper.selectById(id);
        if (finding == null) {
            return ResponseResult.error("审计发现不存在");
        }
        if (!"reviewing".equals(finding.getStatus())) {
            return ResponseResult.error("非法状态流转：仅待复核(reviewing)可复核验证，当前[" + finding.getStatus() + "]");
        }
        Object passedObj = body == null ? null : body.get("passed");
        boolean passed = Boolean.TRUE.equals(passedObj) || "true".equals(String.valueOf(passedObj));
        Object reviewer = body == null ? null : body.get("reviewer");
        Object comment = body == null ? null : body.get("reviewComment");
        finding.setReviewer(reviewer == null ? null : String.valueOf(reviewer));
        finding.setReviewComment(comment == null ? null : String.valueOf(comment));
        finding.setReviewTime(LocalDateTime.now());
        if (passed) {
            finding.setStatus("closed");
            finding.setCloseTime(LocalDateTime.now());
            finding.setResolveTime(LocalDateTime.now());
        } else {
            finding.setStatus("correcting");
            finding.setRectifyTime(null);
        }
        finding.setUpdateTime(LocalDateTime.now());
        findingMapper.updateById(finding);
        return ResponseResult.success(finding);
    }

    /** 历史兼容：一键完成整改（直接闭环），终态不再变化 */
    @PutMapping("/{id}/resolve")
    public ResponseResult<AuditFinding> resolve(@PathVariable Long id) {
        AuditFinding finding = findingMapper.selectById(id);
        if (finding == null) {
            return ResponseResult.error("审计发现不存在");
        }
        if (CLOSED_STATES.contains(finding.getStatus())) {
            return ResponseResult.error("该问题已闭环关闭，无需重复操作");
        }
        finding.setStatus("closed");
        finding.setResolveTime(LocalDateTime.now());
        finding.setCloseTime(LocalDateTime.now());
        finding.setUpdateTime(LocalDateTime.now());
        findingMapper.updateById(finding);
        return ResponseResult.success(finding);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(findingMapper.deleteById(id) > 0);
    }
}
