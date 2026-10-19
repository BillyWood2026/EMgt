package com.example.orgmgt.adapter;

import com.example.orgmgt.entity.Organization;
import com.example.orgmgt.mapper.OrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Local IAM Permission Adapter (Fallback Implementation)
 *
 * Provides local fallback implementation when platform IAM middleware is unavailable.
 * Uses legacy role-permission mappings for permission checking.
 *
 * In production, this should be replaced by a Feign client or gRPC client
 * that calls the actual platform IAM service.
 */
@Component
public class LocalIamPermissionAdapter implements IamPermissionAdapter {

    @Autowired
    private OrganizationMapper organizationMapper;

    // Local role-permission mappings (fallback only)
    private static final Map<String, List<String>> ROLE_MODULE_PERMISSIONS = new HashMap<>();
    private static final Map<String, String> ROLE_DATA_SCOPE = new HashMap<>();

    static {
        ROLE_MODULE_PERMISSIONS.put("SUPER_ADMIN", Arrays.asList(
            "ORG:CREATE", "ORG:EDIT", "ORG:DELETE", "ORG:VIEW", "ORG:APPROVE", "ORG:CONFIG",
            "POSITION:CREATE", "POSITION:EDIT", "POSITION:DELETE", "POSITION:VIEW", "POSITION:APPROVE",
            "STAFFING:CREATE", "STAFFING:EDIT", "STAFFING:VIEW", "STAFFING:APPROVE", "STAFFING:CONFIG",
            "COMMAND_CHAIN:CREATE", "COMMAND_CHAIN:EDIT", "COMMAND_CHAIN:DELETE", "COMMAND_CHAIN:VIEW", "COMMAND_CHAIN:APPROVE", "COMMAND_CHAIN:CONFIG",
            "VIRTUAL_ORG:CREATE", "VIRTUAL_ORG:EDIT", "VIRTUAL_ORG:DELETE", "VIRTUAL_ORG:VIEW", "VIRTUAL_ORG:APPROVE",
            "JOB_DESCRIPTION:CREATE", "JOB_DESCRIPTION:EDIT", "JOB_DESCRIPTION:VIEW", "JOB_DESCRIPTION:APPROVE", "JOB_DESCRIPTION:EXPORT",
            "JOB_POSITION_LEVEL:CREATE", "JOB_POSITION_LEVEL:EDIT", "JOB_POSITION_LEVEL:VIEW", "JOB_POSITION_LEVEL:APPROVE", "JOB_POSITION_LEVEL:CONFIG",
            "PERMISSION:MANAGE"
        ));
        ROLE_MODULE_PERMISSIONS.put("HR_ADMIN", Arrays.asList(
            "ORG:CREATE", "ORG:EDIT", "ORG:DELETE", "ORG:VIEW", "ORG:APPROVE", "ORG:CONFIG",
            "POSITION:CREATE", "POSITION:EDIT", "POSITION:DELETE", "POSITION:VIEW", "POSITION:APPROVE",
            "STAFFING:CREATE", "STAFFING:EDIT", "STAFFING:VIEW", "STAFFING:APPROVE", "STAFFING:CONFIG",
            "COMMAND_CHAIN:CREATE", "COMMAND_CHAIN:EDIT", "COMMAND_CHAIN:DELETE", "COMMAND_CHAIN:VIEW", "COMMAND_CHAIN:APPROVE", "COMMAND_CHAIN:CONFIG",
            "VIRTUAL_ORG:CREATE", "VIRTUAL_ORG:EDIT", "VIRTUAL_ORG:DELETE", "VIRTUAL_ORG:VIEW", "VIRTUAL_ORG:APPROVE",
            "JOB_DESCRIPTION:CREATE", "JOB_DESCRIPTION:EDIT", "JOB_DESCRIPTION:VIEW", "JOB_DESCRIPTION:APPROVE", "JOB_DESCRIPTION:EXPORT",
            "JOB_POSITION_LEVEL:CREATE", "JOB_POSITION_LEVEL:EDIT", "JOB_POSITION_LEVEL:VIEW", "JOB_POSITION_LEVEL:APPROVE"
        ));
        ROLE_MODULE_PERMISSIONS.put("COMPANY_LEADER", Arrays.asList(
            "ORG:VIEW", "ORG:APPROVE",
            "POSITION:VIEW", "POSITION:APPROVE",
            "STAFFING:VIEW", "STAFFING:APPROVE",
            "COMMAND_CHAIN:VIEW", "COMMAND_CHAIN:APPROVE",
            "VIRTUAL_ORG:VIEW", "VIRTUAL_ORG:APPROVE",
            "JOB_DESCRIPTION:VIEW", "JOB_DESCRIPTION:APPROVE",
            "JOB_POSITION_LEVEL:VIEW", "JOB_POSITION_LEVEL:APPROVE"
        ));
        ROLE_MODULE_PERMISSIONS.put("DEPT_HEAD", Arrays.asList(
            "ORG:VIEW", "ORG:EDIT",
            "POSITION:VIEW", "POSITION:EDIT",
            "STAFFING:VIEW", "STAFFING:EDIT",
            "COMMAND_CHAIN:VIEW", "COMMAND_CHAIN:CREATE",
            "VIRTUAL_ORG:VIEW", "VIRTUAL_ORG:CREATE",
            "JOB_DESCRIPTION:VIEW", "JOB_DESCRIPTION:APPROVE",
            "JOB_POSITION_LEVEL:VIEW"
        ));
        ROLE_MODULE_PERMISSIONS.put("SECTION_HEAD", Arrays.asList(
            "ORG:VIEW",
            "POSITION:VIEW", "POSITION:EDIT",
            "STAFFING:VIEW",
            "COMMAND_CHAIN:VIEW",
            "JOB_DESCRIPTION:VIEW",
            "JOB_POSITION_LEVEL:VIEW"
        ));
        ROLE_MODULE_PERMISSIONS.put("EMPLOYEE", Arrays.asList(
            "ORG:VIEW",
            "POSITION:VIEW",
            "JOB_DESCRIPTION:VIEW",
            "JOB_POSITION_LEVEL:VIEW"
        ));

        ROLE_DATA_SCOPE.put("SUPER_ADMIN", "ALL");
        ROLE_DATA_SCOPE.put("HR_ADMIN", "ALL");
        ROLE_DATA_SCOPE.put("COMPANY_LEADER", "ALL");
        ROLE_DATA_SCOPE.put("DEPT_HEAD", "OWN_DEPT");
        ROLE_DATA_SCOPE.put("SECTION_HEAD", "OWN_SECTION");
        ROLE_DATA_SCOPE.put("EMPLOYEE", "PERSONAL");
    }

    @Override
    public boolean checkPermission(String userId, String moduleCode, String functionCode) {
        List<String> roles = getUserRoles(userId);
        String permissionKey = moduleCode.toUpperCase() + ":" + functionCode.toUpperCase();
        for (String role : roles) {
            List<String> permissions = ROLE_MODULE_PERMISSIONS.get(role);
            if (permissions != null && permissions.contains(permissionKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PermissionSummary getPermissionSummary(String userId) {
        List<String> roles = getUserRoles(userId);
        List<String> effectivePermissions = new ArrayList<>();
        Set<String> permissionSet = new HashSet<>();

        for (String role : roles) {
            List<String> rolePermissions = ROLE_MODULE_PERMISSIONS.get(role);
            if (rolePermissions != null) {
                for (String perm : rolePermissions) {
                    if (!permissionSet.contains(perm)) {
                        permissionSet.add(perm);
                        effectivePermissions.add(perm);
                    }
                }
            }
        }

        String dataScope = getDataScope(roles);
        return new PermissionSummary(userId, effectivePermissions, dataScope, roles);
    }

    @Override
    public String getDataScopeFilter(String userId, String moduleCode) {
        List<String> roles = getUserRoles(userId);
        return getDataScope(roles);
    }

    @Override
    public List<Long> getAccessibleOrgIds(String userId) {
        List<String> roles = getUserRoles(userId);
        String dataScope = getDataScope(roles);
        Long userOrgId = getUserOrgId(userId);

        switch (dataScope) {
            case "ALL":
                return getAllOrgIds();
            case "OWN_DEPT":
            case "OWN_SECTION":
                return getOrgAndDescendants(userOrgId);
            case "PERSONAL":
                return userOrgId != null ? Arrays.asList(userOrgId) : new ArrayList<>();
            default:
                return new ArrayList<>();
        }
    }

    @Override
    public void refreshPermission(String userId, String changeType) {
        // Local fallback: no-op (permissions are static)
        // In production, this triggers IAM to refresh user's permission cache
    }

    @Override
    public List<Map<String, Object>> getRoleList() {
        List<Map<String, Object>> roles = new ArrayList<>();
        Map<String, String> roleNames = new LinkedHashMap<>();
        roleNames.put("SUPER_ADMIN", "Super Administrator");
        roleNames.put("HR_ADMIN", "HR Organization Admin");
        roleNames.put("COMPANY_LEADER", "Company Level Leader");
        roleNames.put("DEPT_HEAD", "Department Head");
        roleNames.put("SECTION_HEAD", "Section/Group Head");
        roleNames.put("EMPLOYEE", "Regular Employee");

        for (Map.Entry<String, String> entry : roleNames.entrySet()) {
            Map<String, Object> role = new HashMap<>();
            role.put("roleCode", entry.getKey());
            role.put("roleName", entry.getValue());
            role.put("dataScope", ROLE_DATA_SCOPE.get(entry.getKey()));
            role.put("source", "LOCAL_FALLBACK");
            roles.add(role);
        }
        return roles;
    }

    @Override
    public List<Map<String, Object>> getModuleFunctionList() {
        List<Map<String, Object>> modules = new ArrayList<>();
        modules.add(createModule("ORG", "Organization Management",
            Arrays.asList("CREATE", "EDIT", "DELETE", "VIEW", "APPROVE", "CONFIG")));
        modules.add(createModule("POSITION", "Position Management",
            Arrays.asList("CREATE", "EDIT", "DELETE", "VIEW", "APPROVE")));
        modules.add(createModule("STAFFING", "Staffing Management",
            Arrays.asList("CREATE", "EDIT", "VIEW", "APPROVE", "CONFIG")));
        modules.add(createModule("COMMAND_CHAIN", "Command Chain Management",
            Arrays.asList("CREATE", "EDIT", "DELETE", "VIEW", "APPROVE", "CONFIG")));
        modules.add(createModule("VIRTUAL_ORG", "Virtual Organization",
            Arrays.asList("CREATE", "EDIT", "DELETE", "VIEW", "APPROVE")));
        modules.add(createModule("JOB_DESCRIPTION", "Job Description",
            Arrays.asList("CREATE", "EDIT", "VIEW", "APPROVE", "EXPORT")));
        modules.add(createModule("JOB_POSITION_LEVEL", "Job Position Level",
            Arrays.asList("CREATE", "EDIT", "VIEW", "APPROVE", "CONFIG")));
        modules.add(createModule("PERMISSION", "Permission Management",
            Arrays.asList("MANAGE")));
        return modules;
    }

    // ==================== Helper Methods ====================

    private List<String> getUserRoles(String userId) {
        if (userId == null) return Arrays.asList("EMPLOYEE");
        if (userId.startsWith("admin")) return Arrays.asList("SUPER_ADMIN");
        if (userId.startsWith("hr")) return Arrays.asList("HR_ADMIN");
        if (userId.startsWith("leader")) return Arrays.asList("COMPANY_LEADER");
        if (userId.startsWith("dept")) return Arrays.asList("DEPT_HEAD");
        if (userId.startsWith("section")) return Arrays.asList("SECTION_HEAD");
        return Arrays.asList("EMPLOYEE");
    }

    private String getDataScope(List<String> roles) {
        if (roles.contains("SUPER_ADMIN") || roles.contains("HR_ADMIN") || roles.contains("COMPANY_LEADER")) {
            return "ALL";
        } else if (roles.contains("DEPT_HEAD")) {
            return "OWN_DEPT";
        } else if (roles.contains("SECTION_HEAD")) {
            return "OWN_SECTION";
        } else {
            return "PERSONAL";
        }
    }

    private List<Long> getAllOrgIds() {
        List<Organization> allOrgs = organizationMapper.findAll();
        List<Long> ids = new ArrayList<>();
        for (Organization org : allOrgs) {
            ids.add(org.getId());
        }
        return ids;
    }

    private List<Long> getOrgAndDescendants(Long orgId) {
        List<Long> result = new ArrayList<>();
        if (orgId == null) return result;
        result.add(orgId);
        List<Organization> children = organizationMapper.findByParentOrgId(orgId);
        for (Organization child : children) {
            result.addAll(getOrgAndDescendants(child.getId()));
        }
        return result;
    }

    private Long getUserOrgId(String userId) {
        if (userId == null) return null;
        if (userId.startsWith("admin") || userId.startsWith("hr") || userId.startsWith("leader")) return 1L;
        if (userId.startsWith("dept")) {
            try {
                String num = userId.replace("dept-", "");
                return 2L + Long.parseLong(num);
            } catch (Exception e) {
                return 2L;
            }
        }
        if (userId.startsWith("section")) {
            try {
                String num = userId.replace("section-", "");
                return 5L + Long.parseLong(num);
            } catch (Exception e) {
                return 5L;
            }
        }
        return 10L;
    }

    private Map<String, Object> createModule(String code, String name, List<String> functions) {
        Map<String, Object> module = new HashMap<>();
        module.put("moduleCode", code);
        module.put("moduleName", name);
        List<Map<String, Object>> funcList = new ArrayList<>();
        for (String func : functions) {
            Map<String, Object> funcMap = new HashMap<>();
            funcMap.put("functionCode", func);
            funcList.add(funcMap);
        }
        module.put("functions", funcList);
        return module;
    }
}
