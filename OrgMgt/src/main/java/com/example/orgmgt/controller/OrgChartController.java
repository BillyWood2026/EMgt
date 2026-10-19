package com.example.orgmgt.controller;

import com.example.common.dto.ResponseResult;
import com.example.orgmgt.entity.JobPositionLevel;
import com.example.orgmgt.entity.Organization;
import com.example.orgmgt.entity.Position;
import com.example.orgmgt.service.JobPositionLevelService;
import com.example.orgmgt.service.OrganizationService;
import com.example.orgmgt.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Organization Chart Visualization Controller.
 * Provides endpoints for three types of org charts required by the SRS:
 * 1. Organization chart (org tree)
 * 2. Position org chart (orgs with positions as children)
 * 3. Personnel org chart (orgs with positions and personnel)
 */
@RestController
@RequestMapping("/api/org/chart")
public class OrgChartController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private JobPositionLevelService jobPositionLevelService;

    /**
     * Organization chart - returns organization tree structure.
     * Delegates to OrganizationService.getOrgTree().
     */
    @GetMapping("/organization")
    public ResponseResult<List<Map<String, Object>>> getOrganizationChart() {
        return ResponseResult.success(organizationService.getOrgTree());
    }

    /**
     * Position org chart - returns organization tree with positions nested under each org.
     * Organization nodes contain positions as children.
     */
    @GetMapping("/position")
    public ResponseResult<List<Map<String, Object>>> getPositionChart() {
        List<Organization> allOrgs = organizationService.findAll();
        List<Organization> roots = allOrgs.stream()
                .filter(o -> o.getParentOrgId() == null || o.getParentOrgId() == 0)
                .collect(Collectors.toList());
        List<Map<String, Object>> tree = roots.stream()
                .map(root -> buildPositionNode(root, allOrgs))
                .collect(Collectors.toList());
        return ResponseResult.success(tree);
    }

    /**
     * Personnel org chart - returns organization tree with positions and personnel.
     * For each position, shows incumbent info from job_position_level table,
     * or a vacant placeholder if no incumbent is appointed.
     */
    @GetMapping("/personnel")
    public ResponseResult<List<Map<String, Object>>> getPersonnelChart() {
        List<Organization> allOrgs = organizationService.findAll();
        List<Organization> roots = allOrgs.stream()
                .filter(o -> o.getParentOrgId() == null || o.getParentOrgId() == 0)
                .collect(Collectors.toList());
        List<Map<String, Object>> tree = roots.stream()
                .map(root -> buildPersonnelNode(root, allOrgs))
                .collect(Collectors.toList());
        return ResponseResult.success(tree);
    }

    /**
     * Returns positions for a specific organization.
     */
    @GetMapping("/position/{orgId}")
    public ResponseResult<List<Position>> getPositionsByOrg(@PathVariable Long orgId) {
        return ResponseResult.success(positionService.findByOrgId(orgId));
    }

    /**
     * Returns all positions with vacant_count > 0 (highlighted vacancies).
     */
    @GetMapping("/vacant-positions")
    public ResponseResult<List<Map<String, Object>>> getVacantPositions() {
        List<Position> positions = positionService.findVacantPositions();
        List<Map<String, Object>> result = positions.stream()
                .filter(pos -> computeVacantCount(pos) > 0)
                .map(this::toVacantPositionNode)
                .collect(Collectors.toList());
        return ResponseResult.success(result);
    }

    // ===== Helper methods for tree building =====

    /**
     * Build an org node with child orgs and positions as children (position chart).
     */
    private Map<String, Object> buildPositionNode(Organization org, List<Organization> allOrgs) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", org.getId());
        node.put("name", org.getOrgName());
        node.put("type", "ORG");
        node.put("level", org.getOrgLevel());
        node.put("orgCode", org.getOrgCode());

        List<Map<String, Object>> children = new ArrayList<>();

        // Append child organizations recursively
        List<Organization> childOrgs = allOrgs.stream()
                .filter(o -> Objects.equals(o.getParentOrgId(), org.getId()))
                .collect(Collectors.toList());
        for (Organization child : childOrgs) {
            children.add(buildPositionNode(child, allOrgs));
        }

        // Append positions under this organization
        List<Position> positions = positionService.findByOrgId(org.getId());
        for (Position pos : positions) {
            children.add(toPositionNode(pos, org.getOrgLevel()));
        }

        node.put("children", children);
        return node;
    }

    /**
     * Build an org node with child orgs, positions, and personnel as children (personnel chart).
     */
    private Map<String, Object> buildPersonnelNode(Organization org, List<Organization> allOrgs) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", org.getId());
        node.put("name", org.getOrgName());
        node.put("type", "ORG");
        node.put("level", org.getOrgLevel());
        node.put("orgCode", org.getOrgCode());

        List<Map<String, Object>> children = new ArrayList<>();

        // Append child organizations recursively
        List<Organization> childOrgs = allOrgs.stream()
                .filter(o -> Objects.equals(o.getParentOrgId(), org.getId()))
                .collect(Collectors.toList());
        for (Organization child : childOrgs) {
            children.add(buildPersonnelNode(child, allOrgs));
        }

        // Append positions with personnel under this organization
        List<Position> positions = positionService.findByOrgId(org.getId());
        for (Position pos : positions) {
            children.add(toPersonnelPositionNode(pos, org.getOrgLevel()));
        }

        node.put("children", children);
        return node;
    }

    /**
     * Build a position node for the position chart (no personnel children).
     */
    private Map<String, Object> toPositionNode(Position pos, Integer orgLevel) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", pos.getId());
        node.put("name", pos.getPositionName());
        node.put("type", "POSITION");
        node.put("level", orgLevel);
        node.put("positionCode", pos.getPositionCode());
        node.put("positionLevel", pos.getPositionLevel());
        node.put("positionStatus", pos.getPositionStatus());
        node.put("staffingCount", pos.getStaffingCount());
        node.put("currentCount", pos.getCurrentCount());
        node.put("vacantCount", computeVacantCount(pos));
        node.put("children", new ArrayList<>());
        return node;
    }

    /**
     * Build a position node with personnel children for the personnel chart.
     * Looks up incumbent info from job_position_level table by position code.
     * Shows a vacant placeholder when no incumbent is appointed.
     */
    private Map<String, Object> toPersonnelPositionNode(Position pos, Integer orgLevel) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", pos.getId());
        node.put("name", pos.getPositionName());
        node.put("type", "POSITION");
        node.put("level", orgLevel);
        node.put("positionCode", pos.getPositionCode());
        node.put("positionLevel", pos.getPositionLevel());
        node.put("positionStatus", pos.getPositionStatus());
        node.put("staffingCount", pos.getStaffingCount());
        node.put("currentCount", pos.getCurrentCount());
        node.put("vacantCount", computeVacantCount(pos));

        List<Map<String, Object>> children = new ArrayList<>();

        // Look up incumbent info from job_position_level table
        JobPositionLevel jpl = jobPositionLevelService.getPositionByCode(pos.getPositionCode());
        if (jpl != null && jpl.getIncumbentName() != null && !jpl.getIncumbentName().isEmpty()) {
            // Incumbent appointed - show person node
            Map<String, Object> personNode = new HashMap<>();
            personNode.put("id", jpl.getIncumbentId());
            personNode.put("name", jpl.getIncumbentName());
            personNode.put("type", "PERSON");
            personNode.put("level", orgLevel);
            personNode.put("status", "FILLED");
            personNode.put("children", new ArrayList<>());
            children.add(personNode);
        } else {
            // No incumbent - show vacant placeholder
            Map<String, Object> vacantNode = new HashMap<>();
            vacantNode.put("id", null);
            vacantNode.put("name", "[Vacant]");
            vacantNode.put("type", "PERSON");
            vacantNode.put("level", orgLevel);
            vacantNode.put("status", "VACANT");
            vacantNode.put("children", new ArrayList<>());
            children.add(vacantNode);
        }

        node.put("children", children);
        return node;
    }

    /**
     * Build a vacant position node for the vacant-positions endpoint.
     */
    private Map<String, Object> toVacantPositionNode(Position pos) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", pos.getId());
        node.put("name", pos.getPositionName());
        node.put("type", "POSITION");
        node.put("orgId", pos.getOrgId());
        node.put("orgName", pos.getOrgName());
        node.put("positionCode", pos.getPositionCode());
        node.put("positionLevel", pos.getPositionLevel());
        node.put("positionStatus", pos.getPositionStatus());
        node.put("staffingCount", pos.getStaffingCount());
        node.put("currentCount", pos.getCurrentCount());
        node.put("vacantCount", computeVacantCount(pos));
        return node;
    }

    /**
     * Compute the vacant count for a position (staffingCount - currentCount).
     * Returns 0 if either value is null.
     */
    private int computeVacantCount(Position pos) {
        if (pos.getStaffingCount() == null || pos.getCurrentCount() == null) {
            return 0;
        }
        return Math.max(0, pos.getStaffingCount() - pos.getCurrentCount());
    }
}
