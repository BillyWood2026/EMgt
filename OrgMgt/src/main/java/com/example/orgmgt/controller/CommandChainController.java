package com.example.orgmgt.controller;

import com.example.orgmgt.entity.CommandChain;
import com.example.orgmgt.entity.CommandChainNode;
import com.example.orgmgt.service.CommandChainService;
import com.example.common.dto.ResponseResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Command Chain Controller
 * Manages command chains for work instruction, approval, and responsibility tracking
 * Supports: Fixed chain (based on org structure), Special chain (custom), Authorized chain (temporary)
 */
@RestController
@RequestMapping("/api/org-mgt/command-chain")
public class CommandChainController {
    
    @Autowired
    private CommandChainService commandChainService;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    /**
     * Accept both {"chain": {...}, "nodes": [...]} and flat entity payloads from the frontend.
     */
    private CommandChain extractChain(Map<String, Object> params) {
        Object chainObj = params.get("chain");
        if (chainObj instanceof Map) {
            return MAPPER.convertValue(chainObj, CommandChain.class);
        }
        if (chainObj instanceof CommandChain) {
            return (CommandChain) chainObj;
        }
        return MAPPER.convertValue(params, CommandChain.class);
    }

    @SuppressWarnings("unchecked")
    private List<CommandChainNode> extractNodes(Map<String, Object> params) {
        Object nodes = params.get("nodes");
        if (nodes instanceof List) {
            return MAPPER.convertValue(nodes,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, CommandChainNode.class));
        }
        return null;
    }

    /**
     * Get command chain list with filters
     */
    @GetMapping("/list")
    public ResponseResult<List<CommandChain>> getChainList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chainType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String business,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<CommandChain> list = commandChainService.getChainList(keyword, chainType, status, business, page, pageSize);
        return ResponseResult.success(list);
    }
    
    /**
     * Get command chain tree structure
     */
    @GetMapping("/tree")
    public ResponseResult<List<Map<String, Object>>> getChainTree() {
        List<Map<String, Object>> tree = commandChainService.getChainTree();
        return ResponseResult.success(tree);
    }
    
    /**
     * Get command chain detail by ID
     */
    @GetMapping("/{id}")
    public ResponseResult<CommandChain> getChainById(@PathVariable Long id) {
        CommandChain chain = commandChainService.getChainById(id);
        return ResponseResult.success(chain);
    }
    
    /**
     * Get chain nodes by chain ID
     */
    @GetMapping("/{id}/nodes")
    public ResponseResult<List<CommandChainNode>> getChainNodes(@PathVariable Long id) {
        List<CommandChainNode> nodes = commandChainService.getChainNodes(id);
        return ResponseResult.success(nodes);
    }
    
    /**
     * Create new command chain (submit for approval)
     */
    @PostMapping
    public ResponseResult<String> createChain(@RequestBody Map<String, Object> params) {
        CommandChain chain = extractChain(params);
        List<CommandChainNode> nodes = extractNodes(params);
        commandChainService.createChain(chain, nodes);
        return ResponseResult.success("Command chain created, approval workflow initiated");
    }
    
    /**
     * Update command chain (submit for approval)
     */
    @PutMapping("/{id}")
    public ResponseResult<String> updateChain(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        CommandChain chain = extractChain(params);
        List<CommandChainNode> nodes = extractNodes(params);
        commandChainService.updateChain(id, chain, nodes);
        return ResponseResult.success("Command chain updated, approval workflow initiated");
    }
    
    /**
     * Save chain as draft
     */
    @PostMapping("/draft")
    public ResponseResult<String> saveDraft(@RequestBody Map<String, Object> params) {
        CommandChain chain = extractChain(params);
        List<CommandChainNode> nodes = extractNodes(params);
        commandChainService.saveDraft(chain, nodes);
        return ResponseResult.success("Draft saved");
    }
    
    /**
     * Delete command chain (logical delete)
     */
    @DeleteMapping("/{id}")
    public ResponseResult<String> deleteChain(@PathVariable Long id) {
        commandChainService.deleteChain(id);
        return ResponseResult.success("Command chain deleted");
    }

    /**
     * Enable/pause command chain
     */
    @PostMapping("/{id}/toggle-status")
    public ResponseResult<String> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String newStatus = (String) params.get("newStatus");
        commandChainService.toggleStatus(id, newStatus);
        return ResponseResult.success("Status changed, approval workflow initiated");
    }
    
    /**
     * Batch enable chains
     */
    @PostMapping("/batch-enable")
    public ResponseResult<String> batchEnable(@RequestBody List<Long> ids) {
        commandChainService.batchEnable(ids);
        return ResponseResult.success("Chains batch enabled, approval workflow initiated");
    }
    
    /**
     * Batch pause chains
     */
    @PostMapping("/batch-pause")
    public ResponseResult<String> batchPause(@RequestBody List<Long> ids) {
        commandChainService.batchPause(ids);
        return ResponseResult.success("Chains batch paused, approval workflow initiated");
    }
    
    /**
     * Extend chain validity period
     */
    @PostMapping("/{id}/extend")
    public ResponseResult<String> extendChain(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String newEndTime = (String) params.get("newEndTime");
        commandChainService.extendChain(id, newEndTime);
        return ResponseResult.success("Extension request submitted for approval");
    }
    
    /**
     * Abandon command chain
     */
    @PostMapping("/{id}/abandon")
    public ResponseResult<String> abandonChain(@PathVariable Long id) {
        commandChainService.abandonChain(id);
        return ResponseResult.success("Abandon request submitted for approval");
    }
    
    /**
     * Validate chain configuration
     */
    @PostMapping("/validate")
    public ResponseResult<Map<String, Object>> validateChain(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = commandChainService.validateChain(params);
        return ResponseResult.success(result);
    }
    
    /**
     * Sync with organization structure
     */
    @PostMapping("/sync-org")
    public ResponseResult<String> syncWithOrg() {
        commandChainService.syncWithOrg();
        return ResponseResult.success("Organization structure synchronized");
    }
    
    /**
     * Get status statistics
     */
    @GetMapping("/stats")
    public ResponseResult<Map<String, Integer>> getStats() {
        Map<String, Integer> stats = commandChainService.getStats();
        return ResponseResult.success(stats);
    }
    
    /**
     * Get change history for command chain
     */
    @GetMapping("/{id}/history")
    public ResponseResult<List<Map<String, Object>>> getChangeHistory(@PathVariable Long id) {
        List<Map<String, Object>> history = commandChainService.getChangeHistory(id);
        return ResponseResult.success(history);
    }
    
    /**
     * Compare versions
     */
    @GetMapping("/{id}/compare")
    public ResponseResult<Map<String, Object>> compareVersions(@PathVariable Long id, @RequestParam Long versionId) {
        Map<String, Object> diff = commandChainService.compareVersions(id, versionId);
        return ResponseResult.success(diff);
    }
    
    /**
     * Export command chain list
     */
    @GetMapping("/export")
    public ResponseResult<String> exportChainList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chainType,
            @RequestParam(required = false) String status) {
        String filePath = commandChainService.exportChainList(keyword, chainType, status);
        return ResponseResult.success(filePath);
    }
    
    /**
     * Export single chain as image/PDF
     */
    @GetMapping("/{id}/export")
    public ResponseResult<String> exportChain(@PathVariable Long id) {
        String filePath = commandChainService.exportChain(id);
        return ResponseResult.success(filePath);
    }
}