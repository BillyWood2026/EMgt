package com.example.orgmgt.service.impl;

import com.example.orgmgt.adapter.IamPermissionAdapter;
import com.example.orgmgt.service.OrgPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Organization Permission Service Implementation
 *
 * Delegates all permission checks to the platform IAM permission middleware
 * via the IamPermissionAdapter. The actual permission rules, role mappings,
 * and data scope configurations are managed by the IAM middleware, NOT by
 * this module.
 *
 * This module does NOT self-build any permission rules. All function
 * permissions and data permissions are uniformly managed by IAM middleware.
 */
@Service
public class OrgPermissionServiceImpl implements OrgPermissionService {

    /**
     * IAM Permission Adapter - delegates to platform IAM middleware
     * Auto-wired with local fallback implementation when platform IAM is unavailable
     */
    @Autowired
    private IamPermissionAdapter iamPermissionAdapter;

    @Override
    public boolean hasPermission(String userId, String moduleCode, String functionCode) {
        return iamPermissionAdapter.checkPermission(userId, moduleCode, functionCode);
    }

    @Override
    public List<Map<String, Object>> getUserPermissions(String userId) {
        IamPermissionAdapter.PermissionSummary summary = iamPermissionAdapter.getPermissionSummary(userId);
        List<Map<String, Object>> permissions = new ArrayList<>();

        if (summary.getEffectivePermissions() != null) {
            for (String perm : summary.getEffectivePermissions()) {
                String[] parts = perm.split(":");
                if (parts.length >= 2) {
                    Map<String, Object> permMap = new HashMap<>();
                    permMap.put("moduleCode", parts[0]);
                    permMap.put("functionCode", parts[1]);
                    permMap.put("source", "IAM");
                    permissions.add(permMap);
                }
            }
        }
        return permissions;
    }

    @Override
    public List<Map<String, Object>> getRolePermissions(String roleCode) {
        // Role permissions are managed by IAM middleware
        // This method returns permissions for the specified role from IAM
        List<Map<String, Object>> permissions = new ArrayList<>();
        List<Map<String, Object>> roleList = iamPermissionAdapter.getRoleList();

        boolean roleExists = false;
        for (Map<String, Object> role : roleList) {
            if (roleCode.equals(role.get("roleCode"))) {
                roleExists = true;
                break;
            }
        }

        if (roleExists) {
            // Return module function list as the role's available permissions
            List<Map<String, Object>> modules = iamPermissionAdapter.getModuleFunctionList();
            for (Map<String, Object> module : modules) {
                Map<String, Object> permMap = new HashMap<>();
                permMap.put("moduleCode", module.get("moduleCode"));
                permMap.put("moduleName", module.get("moduleName"));
                permMap.put("functions", module.get("functions"));
                permMap.put("source", "IAM");
                permissions.add(permMap);
            }
        }
        return permissions;
    }

    @Override
    public void grantPermission(String roleCode, String moduleCode, String functionCode, String dataScope) {
        throw new IllegalStateException(
            "Permission grant operation must be performed through IAM middleware admin console. " +
            "Parameters: roleCode=" + roleCode + ", moduleCode=" + moduleCode + 
            ", functionCode=" + functionCode + ", dataScope=" + dataScope);
    }

    @Override
    public void revokePermission(String roleCode, String moduleCode, String functionCode) {
        throw new IllegalStateException(
            "Permission revoke operation must be performed through IAM middleware admin console. " +
            "Parameters: roleCode=" + roleCode + ", moduleCode=" + moduleCode + 
            ", functionCode=" + functionCode);
    }

    @Override
    public void batchUpdatePermissions(String roleCode, List<Map<String, Object>> permissions) {
        throw new IllegalStateException(
            "Permission batch update operation must be performed through IAM middleware admin console. " +
            "Parameters: roleCode=" + roleCode + ", permissionCount=" + (permissions != null ? permissions.size() : 0));
    }

    @Override
    public List<Long> getAccessibleOrgIds(String userId) {
        return iamPermissionAdapter.getAccessibleOrgIds(userId);
    }

    @Override
    public boolean canAccessOrg(String userId, Long orgId) {
        List<Long> accessibleOrgs = getAccessibleOrgIds(userId);
        return accessibleOrgs.contains(orgId);
    }

    @Override
    public List<Map<String, Object>> getRoleList() {
        return iamPermissionAdapter.getRoleList();
    }

    @Override
    public List<Map<String, Object>> getModuleFunctionList() {
        return iamPermissionAdapter.getModuleFunctionList();
    }

    @Override
    public Map<String, Object> validateOperation(String userId, String moduleCode, String functionCode, Long orgId) {
        Map<String, Object> result = new HashMap<>();

        // Check function permission via IAM
        boolean hasPerm = hasPermission(userId, moduleCode, functionCode);

        // Check data scope via IAM
        boolean canAccess = true;
        if (orgId != null) {
            canAccess = canAccessOrg(userId, orgId);
        }

        result.put("hasPermission", hasPerm);
        result.put("canAccessOrg", canAccess);
        result.put("allowed", hasPerm && canAccess);

        if (!hasPerm) {
            result.put("message", "Insufficient permission for this operation");
        } else if (!canAccess) {
            result.put("message", "Cannot access the specified organization");
        }

        return result;
    }
}
