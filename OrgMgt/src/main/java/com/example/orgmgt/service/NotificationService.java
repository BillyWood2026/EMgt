package com.example.orgmgt.service;

import com.example.orgmgt.entity.OrgApproval;
import com.example.orgmgt.entity.VirtualOrganization;

import java.util.List;
import java.util.Map;

/**
 * COMM-005: Notification Service Interface
 * Unified warning and notification push mechanism for:
 * - Expiry warnings (virtual org, flexible staffing, authorization)
 * - Overstaffing alerts (red/yellow/green three-color warning)
 * - Management span anomalies
 * - Approval timeout escalation
 * - Home dashboard warning panel aggregation
 * - Internal message (站内信) sync
 * - OA todo sync
 */
public interface NotificationService {

    /**
     * Send virtual organization expiry warning
     */
    void sendVirtualOrgExpiryWarning(VirtualOrganization virtualOrg, long daysRemaining);

    /**
     * Send virtual organization expiry notification (auto-disband)
     */
    void sendVirtualOrgExpiryNotification(VirtualOrganization virtualOrg);

    /**
     * Send approval timeout warning (approaching deadline)
     */
    void sendApprovalTimeoutWarning(OrgApproval approval, long hoursRemaining);

    /**
     * Send approval timeout escalation notification
     */
    void sendApprovalTimeoutEscalation(OrgApproval approval, String escalatedApprover);

    /**
     * Send staffing over-quota alert (red warning)
     */
    void sendStaffingOverQuotaAlert(String orgCode, String orgName, String positionName, 
                                    int currentCount, int maxCount);

    /**
     * Send management span anomaly alert
     */
    void sendManagementSpanAlert(String orgCode, String orgName, String positionLevel,
                                 int currentSpan, int minSpan, int maxSpan, String warningLevel);

    /**
     * Send authorization expiry warning
     */
    void sendAuthorizationExpiryWarning(String authType, String fromUser, String toUser,
                                        int daysRemaining);

    /**
     * Get warning panel data for home dashboard
     */
    Map<String, Object> getWarningPanelData(String userId);

    /**
     * Get unread notifications for user
     */
    List<Map<String, Object>> getUnreadNotifications(String userId);

    /**
     * Mark notification as read
     */
    void markAsRead(Long notificationId);

    /**
     * Batch mark notifications as read
     */
    void batchMarkAsRead(List<Long> notificationIds);
}
