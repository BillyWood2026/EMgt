package com.example.orgmgt.service;

import com.example.orgmgt.entity.CommandChain;
import com.example.orgmgt.entity.CommandChainNode;
import java.util.List;
import java.util.Map;

/**
 * Command Chain Service Interface
 * Provides business logic for command chain management
 */
public interface CommandChainService {
    
    /**
     * Get command chain list with filters
     */
    List<CommandChain> getChainList(String keyword, String chainType, String status, String business, Integer page, Integer pageSize);
    
    /**
     * Get command chain tree structure
     */
    List<Map<String, Object>> getChainTree();
    
    /**
     * Get command chain detail by ID
     */
    CommandChain getChainById(Long id);
    
    /**
     * Get chain nodes by chain ID
     */
    List<CommandChainNode> getChainNodes(Long id);
    
    /**
     * Create new command chain (submit for approval)
     * Approval flow: Org Apply -> HR Review -> Leader Approve
     */
    void createChain(CommandChain chain, List<CommandChainNode> nodes);
    
    /**
     * Update command chain (submit for approval)
     */
    void updateChain(Long id, CommandChain chain, List<CommandChainNode> nodes);
    
    /**
     * Save chain as draft
     */
    void saveDraft(CommandChain chain, List<CommandChainNode> nodes);

    /**
     * Delete (logical) a command chain by ID
     */
    void deleteChain(Long id);
    
    /**
     * Enable/pause command chain
     */
    void toggleStatus(Long id, String newStatus);
    
    /**
     * Batch enable chains
     */
    void batchEnable(List<Long> ids);
    
    /**
     * Batch pause chains
     */
    void batchPause(List<Long> ids);
    
    /**
     * Extend chain validity period
     */
    void extendChain(Long id, String newEndTime);
    
    /**
     * Abandon command chain
     */
    void abandonChain(Long id);
    
    /**
     * Validate chain configuration
     * Check for conflicts, missing nodes, invalid permissions
     */
    Map<String, Object> validateChain(Map<String, Object> params);
    
    /**
     * Sync with organization structure
     * Update fixed chains based on current org structure
     */
    void syncWithOrg();
    
    /**
     * Get status statistics
     */
    Map<String, Integer> getStats();
    
    /**
     * Get change history for command chain
     */
    List<Map<String, Object>> getChangeHistory(Long id);
    
    /**
     * Compare versions
     */
    Map<String, Object> compareVersions(Long id, Long versionId);
    
    /**
     * Export command chain list
     */
    String exportChainList(String keyword, String chainType, String status);
    
    /**
     * Export single chain as image/PDF
     */
    String exportChain(Long id);
}