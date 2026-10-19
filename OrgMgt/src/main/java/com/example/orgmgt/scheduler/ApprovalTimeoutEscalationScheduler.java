package com.example.orgmgt.scheduler;

import com.example.orgmgt.entity.OrgApproval;
import com.example.orgmgt.mapper.OrgApprovalMapper;
import com.example.orgmgt.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * CHAIN-004: Approval Timeout Escalation Scheduler
 * Monitors pending approvals for timeout and handles escalation:
 * - Marks node as red/timeout when exceeding time limit
 * - Auto-escalates to next-level supervisor for督办
 * - Sends notifications via站内信/OA reminders
 */
@Component
public class ApprovalTimeoutEscalationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalTimeoutEscalationScheduler.class);

    @Autowired
    private OrgApprovalMapper orgApprovalMapper;

    @Autowired
    private NotificationService notificationService;

    /**
     * Default timeout threshold in hours (24 hours)
     */
    private static final int DEFAULT_TIMEOUT_HOURS = 24;

    /**
     * Warning threshold before timeout (2 hours before)
     */
    private static final int WARNING_HOURS = 2;

    /**
     * Check every hour for timeout approvals
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void checkApprovalTimeout() {
        logger.info("Starting approval timeout check at {}", LocalDateTime.now());

        // Step 1: Handle approvals that have exceeded timeout
        handleTimeoutApprovals();

        // Step 2: Send warnings for approvals approaching timeout
        handleApproachingTimeoutApprovals();

        logger.info("Approval timeout check completed");
    }

    /**
     * Handles approvals that have exceeded the timeout threshold.
     * Escalates to next-level supervisor and records escalation history.
     */
    private void handleTimeoutApprovals() {
        List<OrgApproval> timeoutApprovals = orgApprovalMapper.findTimeoutApprovals(DEFAULT_TIMEOUT_HOURS);

        if (timeoutApprovals.isEmpty()) {
            logger.info("No timeout approvals found");
            return;
        }

        logger.info("Found {} approvals that have exceeded {} hours timeout",
            timeoutApprovals.size(), DEFAULT_TIMEOUT_HOURS);

        for (OrgApproval approval : timeoutApprovals) {
            try {
                long hoursElapsed = ChronoUnit.HOURS.between(approval.getSubmitTime(), LocalDateTime.now());

                // Mark node as timeout (red status)
                // In real implementation, this would update a separate timeout tracking table
                // For now, we record it in the operation log
                String timeoutLog = String.format(
                    "[超时督办] 审批单%s在节点[%s]滞留超过%d小时，当前已%d小时。",
                    approval.getApprovalNo(),
                    approval.getCurrentNode(),
                    DEFAULT_TIMEOUT_HOURS,
                    hoursElapsed
                );

                // Escalate to next-level supervisor
                // In production, this would query command chain to find next level approver
                String escalatedApprover = escalateToNextLevel(approval);

                if (escalatedApprover != null) {
                    // Update current approver to escalated supervisor
                    orgApprovalMapper.updateCurrentNode(
                        approval.getId(),
                        approval.getCurrentNode() + "_ESCALATED",
                        escalatedApprover
                    );

                    timeoutLog += String.format(" 已升级至上级督办[%s]", escalatedApprover);

                    logger.info("Escalated approval {} to supervisor {} after {} hours",
                        approval.getApprovalNo(), escalatedApprover, hoursElapsed);
                } else {
                    timeoutLog += " 未能找到上级督办人，请人工介入处理。";
                    logger.warn("Could not find escalation approver for approval {}", approval.getApprovalNo());
                }

                // Update operation log
                String existingLog = approval.getOperationLog();
                String updatedLog = (existingLog != null ? existingLog + "\n" : "") + timeoutLog;
                approval.setOperationLog(updatedLog);
                approval.setUpdateTime(LocalDateTime.now());
                orgApprovalMapper.update(approval);

                // COMM-005: Send timeout escalation notification
                notificationService.sendApprovalTimeoutEscalation(approval, escalatedApprover);

            } catch (Exception e) {
                logger.error("Failed to process timeout escalation for approval {}: {}",
                    approval.getApprovalNo(), e.getMessage());
            }
        }
    }

    /**
     * Handles approvals approaching timeout (within warning threshold).
     * Sends reminder notifications to current approver.
     */
    private void handleApproachingTimeoutApprovals() {
        List<OrgApproval> approachingApprovals = orgApprovalMapper.findApproachingTimeoutApprovals(
            DEFAULT_TIMEOUT_HOURS, WARNING_HOURS);

        if (approachingApprovals.isEmpty()) {
            logger.info("No approvals approaching timeout");
            return;
        }

        logger.info("Found {} approvals approaching timeout within {} hours",
            approachingApprovals.size(), WARNING_HOURS);

        for (OrgApproval approval : approachingApprovals) {
            try {
                long hoursRemaining = DEFAULT_TIMEOUT_HOURS -
                    ChronoUnit.HOURS.between(approval.getSubmitTime(), LocalDateTime.now());

                logger.info("Approval {} approaching timeout, {} hours remaining",
                    approval.getApprovalNo(), hoursRemaining);

                // COMM-005: Send reminder notification to current approver
                notificationService.sendApprovalTimeoutWarning(approval, hoursRemaining);

            } catch (Exception e) {
                logger.error("Failed to process timeout warning for approval {}: {}",
                    approval.getApprovalNo(), e.getMessage());
            }
        }
    }

    /**
     * Escalates to next-level supervisor based on command chain.
     * In production, this would query the command chain service to find the next level approver.
     *
     * @param approval The approval request
     * @return Next-level approver ID, or null if not found
     */
    private String escalateToNextLevel(OrgApproval approval) {
        // Current approver
        String currentApprover = approval.getCurrentApprover();

        if (currentApprover == null) {
            return null;
        }

        // In a real implementation, this would:
        // 1. Get the current approver's position from employee service
        // 2. Query command chain to find the next level supervisor
        // 3. Return the supervisor's user ID

        // For demo, return a mock escalated approver
        // Format: if current is "approver-001", return "supervisor-001"
        if (currentApprover.contains("approver")) {
            return currentApprover.replace("approver", "supervisor");
        } else if (currentApprover.contains("manager")) {
            return currentApprover.replace("manager", "director");
        } else if (currentApprover.contains("director")) {
            return currentApprover.replace("director", "gm");
        }

        // If already at top level, return null (requires manual intervention)
        return null;
    }
}
