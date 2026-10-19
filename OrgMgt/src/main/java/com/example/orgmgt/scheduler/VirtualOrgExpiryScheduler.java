package com.example.orgmgt.scheduler;

import com.example.orgmgt.entity.VirtualOrganization;
import com.example.orgmgt.mapper.VirtualOrganizationMapper;
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
 * VIR-002: Virtual Organization Expiry Scheduler
 * Monitors virtual organizations for expiration and handles:
 * - Yellow warning when approaching expiry (7 days before)
 * - Automatic disbandment when expired
 * - Recording history for audit trail
 */
@Component
public class VirtualOrgExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(VirtualOrgExpiryScheduler.class);

    @Autowired
    private VirtualOrganizationMapper virtualOrganizationMapper;

    @Autowired
    private NotificationService notificationService;

    /**
     * Warning threshold days before expiry
     */
    private static final int WARNING_DAYS = 7;

    /**
     * Daily check at 2:00 AM for virtual organization expiration
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkVirtualOrgExpiry() {
        logger.info("Starting virtual organization expiry check at {}", LocalDateTime.now());

        // Step 1: Handle organizations that have already expired
        handleExpiredOrganizations();

        // Step 2: Send warnings for organizations approaching expiry
        handleExpiringOrganizations();

        logger.info("Virtual organization expiry check completed");
    }

    /**
     * Handles virtual organizations that have passed their expiry date.
     * Automatically changes status to DISBAND and records history.
     */
    private void handleExpiredOrganizations() {
        List<VirtualOrganization> expiredOrgs = virtualOrganizationMapper.selectExpiredOrgs();

        if (expiredOrgs.isEmpty()) {
            logger.info("No expired virtual organizations found");
            return;
        }

        logger.info("Found {} expired virtual organizations to disband", expiredOrgs.size());

        for (VirtualOrganization org : expiredOrgs) {
            try {
                // Update status to DISBAND
                virtualOrganizationMapper.updateStatus(org.getId(), VirtualOrganization.STATUS_DISBAND);

                // Record disbandment history
                String changeContent = String.format(
                    "虚拟组织[%s]于%s到期，系统自动解散",
                    org.getVirtualOrgName(),
                    org.getExpireDate()
                );
                virtualOrganizationMapper.insertChangeHistory(
                    org.getId(),
                    "AUTO_DISBAND",
                    changeContent,
                    "SYSTEM",
                    LocalDateTime.now()
                );

                // COMM-005: Send notification to leader
                notificationService.sendVirtualOrgExpiryNotification(org);

                logger.info("Auto-disbanded virtual organization: {} (ID: {})",
                    org.getVirtualOrgCode(), org.getId());

            } catch (Exception e) {
                logger.error("Failed to auto-disband virtual organization {}: {}",
                    org.getVirtualOrgCode(), e.getMessage());
            }
        }
    }

    /**
     * Handles virtual organizations approaching expiry.
     * Sends yellow warning and updates status to EXPIRING.
     */
    private void handleExpiringOrganizations() {
        List<VirtualOrganization> expiringOrgs = virtualOrganizationMapper.selectExpiringOrgs(WARNING_DAYS);

        if (expiringOrgs.isEmpty()) {
            logger.info("No virtual organizations approaching expiry");
            return;
        }

        logger.info("Found {} virtual organizations expiring within {} days",
            expiringOrgs.size(), WARNING_DAYS);

        for (VirtualOrganization org : expiringOrgs) {
            try {
                long daysUntilExpiry = ChronoUnit.DAYS.between(
                    LocalDateTime.now(), org.getExpireDate());

                // Update status to EXPIRING if not already
                if (!VirtualOrganization.STATUS_EXPIRING.equals(org.getOrgStatus())) {
                    virtualOrganizationMapper.updateStatus(org.getId(), VirtualOrganization.STATUS_EXPIRING);
                }

                // Record warning history
                String changeContent = String.format(
                    "虚拟组织[%s]即将到期，剩余%d天，发送黄色预警",
                    org.getVirtualOrgName(),
                    daysUntilExpiry
                );
                virtualOrganizationMapper.insertChangeHistory(
                    org.getId(),
                    "EXPIRY_WARNING",
                    changeContent,
                    "SYSTEM",
                    LocalDateTime.now()
                );

                // COMM-005: Send warning notification to leader
                notificationService.sendVirtualOrgExpiryWarning(org, daysUntilExpiry);

                logger.info("Sent expiry warning for virtual organization: {} ({} days remaining)",
                    org.getVirtualOrgCode(), daysUntilExpiry);

            } catch (Exception e) {
                logger.error("Failed to process expiry warning for virtual organization {}: {}",
                    org.getVirtualOrgCode(), e.getMessage());
            }
        }
    }
}
