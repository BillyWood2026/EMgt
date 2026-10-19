package com.example.orgmgt.service.impl;

import com.example.orgmgt.entity.OrgApproval;
import com.example.orgmgt.entity.VirtualOrganization;
import com.example.orgmgt.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * COMM-005: Notification Service Implementation
 * Provides unified warning message push for OrgMgt module
 * 
 * Supported channels:
 * - Internal message (站内信)
 * - OA todo sync
 * - Home dashboard warning panel
 * 
 * Warning types:
 * - EXPIRY: Virtual org expiry, flexible staffing expiry, authorization expiry
 * - OVER_QUOTA: Staffing over quota (red)
 * - SPAN_ANOMALY: Management span below minimum (red/yellow)
 * - TIMEOUT: Approval timeout escalation
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    /**
     * In-memory notification store for demo
     * In production, this should use a database table
     */
    private static final Map<Long, Map<String, Object>> NOTIFICATION_STORE = new ConcurrentHashMap<>();
    private static final AtomicLong NOTIFICATION_ID_GENERATOR = new AtomicLong(1);

    // Warning level constants
    private static final String LEVEL_RED = "RED";
    private static final String LEVEL_YELLOW = "YELLOW";
    private static final String LEVEL_GREEN = "GREEN";

    // Notification type constants
    private static final String TYPE_EXPIRY = "EXPIRY";
    private static final String TYPE_OVER_QUOTA = "OVER_QUOTA";
    private static final String TYPE_SPAN_ANOMALY = "SPAN_ANOMALY";
    private static final String TYPE_TIMEOUT = "TIMEOUT";

    @Override
    public void sendVirtualOrgExpiryWarning(VirtualOrganization virtualOrg, long daysRemaining) {
        String title = String.format("虚拟组织[%s]即将到期", virtualOrg.getVirtualOrgName());
        String content = String.format(
            "虚拟组织[%s]（编码：%s）将于%d天后到期，请及时处理续期或归档事宜。",
            virtualOrg.getVirtualOrgName(),
            virtualOrg.getVirtualOrgCode(),
            daysRemaining
        );

        // Send to leader
        createNotification(
            virtualOrg.getLeaderId(),
            TYPE_EXPIRY,
            LEVEL_YELLOW,
            title,
            content,
            "VIRTUAL_ORG",
            virtualOrg.getId()
        );

        logger.info("Sent virtual org expiry warning: {} ({} days remaining)",
            virtualOrg.getVirtualOrgCode(), daysRemaining);
    }

    @Override
    public void sendVirtualOrgExpiryNotification(VirtualOrganization virtualOrg) {
        String title = String.format("虚拟组织[%s]已到期自动解散", virtualOrg.getVirtualOrgName());
        String content = String.format(
            "虚拟组织[%s]（编码：%s）已于%s到期，系统已自动将其状态变更为已解散。",
            virtualOrg.getVirtualOrgName(),
            virtualOrg.getVirtualOrgCode(),
            virtualOrg.getExpireDate()
        );

        // Send to leader
        createNotification(
            virtualOrg.getLeaderId(),
            TYPE_EXPIRY,
            LEVEL_YELLOW,
            title,
            content,
            "VIRTUAL_ORG",
            virtualOrg.getId()
        );

        logger.info("Sent virtual org expiry notification: {}", virtualOrg.getVirtualOrgCode());
    }

    @Override
    public void sendApprovalTimeoutWarning(OrgApproval approval, long hoursRemaining) {
        String title = String.format("审批单[%s]即将超时", approval.getApprovalNo());
        String content = String.format(
            "您有审批单[%s]（业务类型：%s）即将在%d小时后超时，请及时处理。",
            approval.getApprovalNo(),
            approval.getBusinessType(),
            hoursRemaining
        );

        // Send to current approver
        createNotification(
            approval.getCurrentApprover(),
            TYPE_TIMEOUT,
            LEVEL_YELLOW,
            title,
            content,
            "APPROVAL",
            approval.getId()
        );

        logger.info("Sent approval timeout warning: {} ({} hours remaining)",
            approval.getApprovalNo(), hoursRemaining);
    }

    @Override
    public void sendApprovalTimeoutEscalation(OrgApproval approval, String escalatedApprover) {
        String title = String.format("审批单[%s]已升级督办", approval.getApprovalNo());
        String content = String.format(
            "审批单[%s]（业务类型：%s）在节点[%s]滞留超时，已升级至[%s]督办。",
            approval.getApprovalNo(),
            approval.getBusinessType(),
            approval.getCurrentNode(),
            escalatedApprover
        );

        // Send to original approver
        createNotification(
            approval.getCurrentApprover(),
            TYPE_TIMEOUT,
            LEVEL_RED,
            title,
            content,
            "APPROVAL",
            approval.getId()
        );

        // Send to escalated approver
        createNotification(
            escalatedApprover,
            TYPE_TIMEOUT,
            LEVEL_RED,
            title,
            content,
            "APPROVAL",
            approval.getId()
        );

        logger.info("Sent approval timeout escalation: {} -> {}",
            approval.getApprovalNo(), escalatedApprover);
    }

    @Override
    public void sendStaffingOverQuotaAlert(String orgCode, String orgName, String positionName,
                                          int currentCount, int maxCount) {
        String title = String.format("[%s]岗位超编红色告警", positionName);
        String content = String.format(
            "组织[%s]（%s）的岗位[%s]当前人数%d人，已超过固定编制上限%d人，禁止新增人员。",
            orgName, orgCode, positionName, currentCount, maxCount
        );

        // Send to HR admin and org leader
        createNotification(
            "hr-admin",
            TYPE_OVER_QUOTA,
            LEVEL_RED,
            title,
            content,
            "STAFFING",
            null
        );

        logger.info("Sent staffing over-quota alert: {} - {}/{}"
, positionName, currentCount, maxCount);
    }

    @Override
    public void sendManagementSpanAlert(String orgCode, String orgName, String positionLevel,
                                       int currentSpan, int minSpan, int maxSpan, String warningLevel) {
        String title;
        String level;

        if (LEVEL_RED.equals(warningLevel)) {
            title = String.format("[%s]管理幅度红色告警", orgName);
            level = LEVEL_RED;
        } else {
            title = String.format("[%s]管理幅度黄色预警", orgName);
            level = LEVEL_YELLOW;
        }

        String content = String.format(
            "组织[%s]（%s）的%s管理幅度当前为%d人，最低要求%d人，最高限制%d人。%s",
            orgName, orgCode, positionLevel, currentSpan, minSpan, maxSpan,
            LEVEL_RED.equals(warningLevel) ? "管理幅度不达标，禁止定编生效。" : "管理幅度临近阈值，请注意整改。"
        );

        createNotification(
            "hr-admin",
            TYPE_SPAN_ANOMALY,
            level,
            title,
            content,
            "STAFFING",
            null
        );

        logger.info("Sent management span alert: {} - level={}", orgName, warningLevel);
    }

    @Override
    public void sendAuthorizationExpiryWarning(String authType, String fromUser, String toUser,
                                              int daysRemaining) {
        String title = String.format("[%s]授权即将到期", authType);
        String content = String.format(
            "[%s]授权（从%s到%s）将于%d天后到期，到期后自动恢复原指挥链路。",
            authType, fromUser, toUser, daysRemaining
        );

        createNotification(
            fromUser,
            TYPE_EXPIRY,
            LEVEL_YELLOW,
            title,
            content,
            "AUTHORIZATION",
            null
        );

        createNotification(
            toUser,
            TYPE_EXPIRY,
            LEVEL_YELLOW,
            title,
            content,
            "AUTHORIZATION",
            null
        );

        logger.info("Sent authorization expiry warning: {} -> {} ({} days)", fromUser, toUser, daysRemaining);
    }

    @Override
    public Map<String, Object> getWarningPanelData(String userId) {
        Map<String, Object> panelData = new HashMap<>();

        // Count by level
        int redCount = 0;
        int yellowCount = 0;
        List<Map<String, Object>> redWarnings = new ArrayList<>();
        List<Map<String, Object>> yellowWarnings = new ArrayList<>();

        for (Map<String, Object> notification : NOTIFICATION_STORE.values()) {
            String recipient = (String) notification.get("recipient");
            if (!userId.equals(recipient)) {
                continue;
            }

            String level = (String) notification.get("level");
            String readStatus = (String) notification.get("readStatus");

            if ("UNREAD".equals(readStatus)) {
                if (LEVEL_RED.equals(level)) {
                    redCount++;
                    redWarnings.add(notification);
                } else if (LEVEL_YELLOW.equals(level)) {
                    yellowCount++;
                    yellowWarnings.add(notification);
                }
            }
        }

        panelData.put("redCount", redCount);
        panelData.put("yellowCount", yellowCount);
        panelData.put("totalCount", redCount + yellowCount);
        panelData.put("redWarnings", redWarnings);
        panelData.put("yellowWarnings", yellowWarnings);

        return panelData;
    }

    @Override
    public List<Map<String, Object>> getUnreadNotifications(String userId) {
        List<Map<String, Object>> unread = new ArrayList<>();

        for (Map<String, Object> notification : NOTIFICATION_STORE.values()) {
            String recipient = (String) notification.get("recipient");
            String readStatus = (String) notification.get("readStatus");

            if (userId.equals(recipient) && "UNREAD".equals(readStatus)) {
                unread.add(notification);
            }
        }

        // Sort by create time desc
        unread.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("createTime");
            LocalDateTime timeB = (LocalDateTime) b.get("createTime");
            return timeB.compareTo(timeA);
        });

        return unread;
    }

    @Override
    public void markAsRead(Long notificationId) {
        Map<String, Object> notification = NOTIFICATION_STORE.get(notificationId);
        if (notification != null) {
            notification.put("readStatus", "READ");
            notification.put("readTime", LocalDateTime.now());
        }
    }

    @Override
    public void batchMarkAsRead(List<Long> notificationIds) {
        for (Long id : notificationIds) {
            markAsRead(id);
        }
    }

    /**
     * Creates a notification record
     */
    private Long createNotification(String recipient, String type, String level,
                                    String title, String content, String sourceModule, Long sourceId) {
        Long id = NOTIFICATION_ID_GENERATOR.getAndIncrement();

        Map<String, Object> notification = new HashMap<>();
        notification.put("id", id);
        notification.put("recipient", recipient);
        notification.put("type", type);
        notification.put("level", level);
        notification.put("title", title);
        notification.put("content", content);
        notification.put("sourceModule", sourceModule);
        notification.put("sourceId", sourceId);
        notification.put("readStatus", "UNREAD");
        notification.put("createTime", LocalDateTime.now());

        NOTIFICATION_STORE.put(id, notification);

        // TODO: In production, also push to:
        // - OA todo system
        // - Email
        // - SMS (for critical alerts)
        // - Enterprise WeChat/DingTalk

        return id;
    }
}
