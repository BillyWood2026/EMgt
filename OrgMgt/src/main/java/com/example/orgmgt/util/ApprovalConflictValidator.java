package com.example.orgmgt.util;

import java.util.Map;

/**
 * PERM-004: Self-approval conflict validator.
 * Prevents initiators from approving their own submitted requests.
 *
 * This validator enforces the segregation of duties principle:
 * the person who submits a request cannot be the same person who approves it.
 */
public class ApprovalConflictValidator {

    /**
     * Validates that the approver is not the same as the initiator.
     *
     * @param initiatorId The user who submitted the request
     * @param approverId The user who is attempting to approve
     * @return true if approval is allowed (different users), false if conflict detected
     */
    public static boolean validateSelfApproval(String initiatorId, String approverId) {
        if (initiatorId == null || approverId == null) {
            return true; // Cannot validate, allow
        }
        return !initiatorId.equals(approverId);
    }

    /**
     * Validates self-approval and throws exception if conflict detected.
     *
     * @param initiatorId The user who submitted the request
     * @param approverId The user who is attempting to approve
     * @throws IllegalStateException if self-approval conflict is detected
     */
    public static void checkSelfApproval(String initiatorId, String approverId) {
        if (!validateSelfApproval(initiatorId, approverId)) {
            throw new IllegalStateException(
                "自审批互斥拦截：发起人[ " + initiatorId + " ]不可审批自身提交的单据，系统已自动流转上级处理");
        }
    }

    /**
     * Validates batch approval to ensure none of the items are self-approved.
     *
     * @param items List of items with initiatorId field
     * @param approverId The user attempting batch approval
     * @param initiatorIdExtractor Function to extract initiatorId from item
     * @param <T> Item type
     * @return Map with "valid" (boolean) and "conflictItems" (List of conflicting items)
     */
    public static <T> Map<String, Object> validateBatchSelfApproval(
            java.util.List<T> items, String approverId,
            java.util.function.Function<T, String> initiatorIdExtractor) {
        java.util.List<T> conflictItems = new java.util.ArrayList<>();

        for (T item : items) {
            String initiatorId = initiatorIdExtractor.apply(item);
            if (!validateSelfApproval(initiatorId, approverId)) {
                conflictItems.add(item);
            }
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("valid", conflictItems.isEmpty());
        result.put("conflictItems", conflictItems);
        result.put("conflictCount", conflictItems.size());

        return result;
    }
}
