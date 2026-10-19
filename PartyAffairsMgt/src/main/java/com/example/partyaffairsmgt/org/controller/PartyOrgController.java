package com.example.partyaffairsmgt.org.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.org.entity.OrgHistory;
import com.example.partyaffairsmgt.org.entity.PartyOrg;
import com.example.partyaffairsmgt.org.mapper.OrgHistoryMapper;
import com.example.partyaffairsmgt.org.service.PartyOrgService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 4.1 组织架构管理接口。 */
@RestController
@RequestMapping("/api/party/org/organization")
public class PartyOrgController {

    @Autowired private PartyOrgService service;
    @Autowired private OrgHistoryMapper historyMapper;

    // ---------- 组织树 / 列表 / 详情 ----------

    @GetMapping("/tree")
    public ResponseResult<List<Map<String, Object>>> tree(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orgType) {
        return ResponseResult.success(service.tree(parentId, keyword, orgType));
    }

    @GetMapping("/list-page")
    public ResponseResult<Map<String, Object>> listPage(
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return ResponseResult.success(service.siblingPage(parentId, pageNum, pageSize, keyword));
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<Map<String, Object>> detail(@PathVariable Long id) {
        return ResponseResult.success(service.detail(id));
    }

    @GetMapping("/members/{orgId}")
    public ResponseResult<Object> members(@PathVariable Long orgId,
                                          @RequestParam(defaultValue = "false") boolean includeSubTree) {
        return ResponseResult.success(service.membersOfOrg(orgId, includeSubTree));
    }

    @GetMapping("/histories/{orgId}")
    public ResponseResult<List<OrgHistory>> histories(@PathVariable Long orgId) {
        return ResponseResult.success(historyMapper.selectList(
                new QueryWrapper<OrgHistory>().eq("org_id", orgId).orderByDesc("change_date", "id")));
    }

    // ---------- 新建 / 编辑 / 审批 / 移动 ----------

    @PostMapping("/save")
    public ResponseResult<Map<String, Object>> create(@RequestBody PartyOrg org,
                                                      @RequestParam(defaultValue = "false") boolean directApprove) {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(service.create(org, directApprove));
    }

    @PutMapping("/update")
    public ResponseResult<Map<String, Object>> update(@RequestBody PartyOrg org) {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(service.update(org));
    }

    @PostMapping("/approve/{id}")
    public ResponseResult<PartyOrg> approve(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        PartyRole.requireAny(PartyRole.R03);
        return ResponseResult.success(service.approve(id, body.get("approvalNo")));
    }

    @PostMapping("/move")
    public ResponseResult<PartyOrg> move(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02);
        Long id = Long.valueOf(body.get("id").toString());
        Long newParentId = body.get("newParentId") == null || body.get("newParentId").toString().isBlank()
                ? null : Long.valueOf(body.get("newParentId").toString());
        return ResponseResult.success(service.move(id, newParentId));
    }

    @PutMapping("/party-groups/{orgId}")
    public ResponseResult<PartyOrg> partyGroups(@PathVariable Long orgId,
                                                @RequestBody Map<String, String> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        return ResponseResult.success(service.updatePartyGroups(orgId, body.get("partyGroups")));
    }

    @GetMapping("/pending")
    public ResponseResult<List<PartyOrg>> pending() {
        return ResponseResult.success(service.pendingList());
    }

    // ---------- 合并 / 撤销 ----------

    @PostMapping("/merge/analyze")
    public ResponseResult<Map<String, Object>> mergeAnalyze(@RequestBody Map<String, Object> body) {
        Long source = Long.valueOf(body.get("sourceOrgId").toString());
        Long target = body.get("targetOrgId") == null ? null
                : Long.valueOf(body.get("targetOrgId").toString());
        return ResponseResult.success(service.impactAnalysis(source, target, "MERGE"));
    }

    @PostMapping("/merge/execute")
    public ResponseResult<Object> mergeExecute(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02);
        Long source = Long.valueOf(body.get("sourceOrgId").toString());
        Long target = Long.valueOf(body.get("targetOrgId").toString());
        return ResponseResult.success(service.executeMerge(source, target, (String) body.get("reason")));
    }

    @PostMapping("/revoke/analyze")
    public ResponseResult<Map<String, Object>> revokeAnalyze(@RequestBody Map<String, Object> body) {
        return ResponseResult.success(
                service.impactAnalysis(Long.valueOf(body.get("orgId").toString()), null, "REVOKE"));
    }

    @PostMapping("/revoke/execute")
    public ResponseResult<Object> revokeExecute(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(service.executeRevoke(
                Long.valueOf(body.get("orgId").toString()), (String) body.get("reason")));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02);
        service.delete(id);
        return ResponseResult.success(true);
    }

    // ---------- 届满提醒 ----------

    @GetMapping("/term-alerts")
    public ResponseResult<Map<String, Object>> termAlerts() {
        return ResponseResult.success(service.termAlerts());
    }

    // ---------- 导出 / 导入 ----------

    @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Long parentId) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03);
        byte[] bytes = service.exportCsv(parentId).getBytes(StandardCharsets.UTF_8);
        String filename = URLEncoder.encode("党组织列表.csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> template() throws IOException {
        byte[] bytes = service.importTemplate();
        String filename = URLEncoder.encode("党组织导入模板.xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping("/import/preview")
    public ResponseResult<Map<String, Object>> importPreview(@RequestParam("file") MultipartFile file)
            throws IOException {
        return ResponseResult.success(service.importPreview(file.getBytes()));
    }

    @PostMapping("/import/commit")
    public ResponseResult<Integer> importCommit(@RequestBody Map<String, List<Map<String, Object>>> body) {
        PartyRole.requireAny(PartyRole.R02);
        List<Map<String, Object>> rows = body.getOrDefault("rows", List.of());
        return ResponseResult.success(service.importCommit(rows));
    }
}
