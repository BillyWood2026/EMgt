package com.example.modconfig.service;

import com.example.modconfig.entity.ModuleConfig;
import com.example.modconfig.repository.ModuleConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleConfigService {

    private final ModuleConfigRepository moduleConfigRepository;

    public List<ModuleConfig> getAllModules() {
        return moduleConfigRepository.findAll();
    }

    public List<ModuleConfig> getEnabledModules() {
        return moduleConfigRepository.findByEnabledTrueOrderByParentModuleKeyAscDisplayOrderAsc();
    }

    public Map<String, List<ModuleConfig>> getModulesGroupedByParent() {
        List<ModuleConfig> enabledModules = getEnabledModules();
        return enabledModules.stream()
                .collect(Collectors.groupingBy(ModuleConfig::getParentModuleKey));
    }

    public List<ModuleConfig> getModulesByParent(String parentModuleKey) {
        return moduleConfigRepository.findByParentModuleKeyAndEnabledTrueOrderByDisplayOrder(parentModuleKey);
    }

    public Optional<ModuleConfig> getModuleBySubKey(String subModuleKey) {
        return moduleConfigRepository.findBySubModuleKey(subModuleKey);
    }

    @Transactional
    public ModuleConfig save(ModuleConfig config) {
        return moduleConfigRepository.save(config);
    }

    @Transactional
    public ModuleConfig createModule(ModuleConfig config) {
        if (moduleConfigRepository.existsBySubModuleKey(config.getSubModuleKey())) {
            throw new IllegalArgumentException("Sub module already exists: " + config.getSubModuleKey());
        }
        return moduleConfigRepository.save(config);
    }

    @Transactional
    public ModuleConfig updateModule(String subModuleKey, ModuleConfig updatedConfig) {
        ModuleConfig existing = moduleConfigRepository.findBySubModuleKey(subModuleKey)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + subModuleKey));
        
        if (updatedConfig.getEnabled() != null) {
            existing.setEnabled(updatedConfig.getEnabled());
        }
        if (updatedConfig.getDisplayOrder() != null) {
            existing.setDisplayOrder(updatedConfig.getDisplayOrder());
        }
        if (updatedConfig.getIcon() != null) {
            existing.setIcon(updatedConfig.getIcon());
        }
        if (updatedConfig.getDescription() != null) {
            existing.setDescription(updatedConfig.getDescription());
        }
        if (updatedConfig.getServiceName() != null) {
            existing.setServiceName(updatedConfig.getServiceName());
        }
        if (updatedConfig.getServicePort() != null) {
            existing.setServicePort(updatedConfig.getServicePort());
        }
        
        return moduleConfigRepository.save(existing);
    }

    @Transactional
    public void deleteModule(String subModuleKey) {
        if (!moduleConfigRepository.existsBySubModuleKey(subModuleKey)) {
            throw new IllegalArgumentException("Module not found: " + subModuleKey);
        }
        moduleConfigRepository.deleteBySubModuleKey(subModuleKey);
    }

    @Transactional
    public ModuleConfig toggleModule(String subModuleKey) {
        ModuleConfig module = moduleConfigRepository.findBySubModuleKey(subModuleKey)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + subModuleKey));
        module.setEnabled(!module.getEnabled());
        return moduleConfigRepository.save(module);
    }

    public Map<String, Object> getModuleNavigationConfig() {
        Map<String, Object> navConfig = new HashMap<>();
        List<ModuleConfig> enabledModules = getEnabledModules();
        
        Map<String, List<Map<String, Object>>> parentGroups = enabledModules.stream()
                .collect(Collectors.groupingBy(
                        ModuleConfig::getParentModuleKey,
                        Collectors.mapping(this::toNavItem, Collectors.toList())
                ));
        
        navConfig.put("modules", parentGroups);
        navConfig.put("totalEnabled", enabledModules.size());
        navConfig.put("totalParentModules", parentGroups.size());
        
        return navConfig;
    }

    private Map<String, Object> toNavItem(ModuleConfig config) {
        Map<String, Object> item = new HashMap<>();
        item.put("subModuleKey", config.getSubModuleKey());
        item.put("subModuleName", config.getSubModuleName());
        item.put("serviceName", config.getServiceName());
        item.put("servicePort", config.getServicePort());
        item.put("icon", config.getIcon());
        item.put("displayOrder", config.getDisplayOrder());
        return item;
    }

    @Transactional
    public void initializeDefaultConfigs() {
        if (moduleConfigRepository.count() > 0) {
            return;
        }

        List<ModuleConfig> defaults = List.of(
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/staff-mgt", "人员管理", "staff-mgt", 8081, true, 1, "UserFilled", "Employee management service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/recruiting", "招聘管理", "recruitment-mgt", 8086, true, 2, "DocumentAdd", "Recruitment management service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/training-mgt", "培训管理", "training-mgt", 8087, true, 3, "Calendar", "Training management service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/perf-mgt", "绩效管理", "perf-mgt", 8083, true, 4, "DataLine", "Performance management service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/qual-rank", "资格等级", "qual-rank", 8084, true, 5, "Medal", "Qualification rank service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/attend-holiday/attendance", "考勤管理", "attendance-mgt", 8083, true, 6, "Clock", "Attendance management service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/attend-holiday/holiday", "假期管理", "holiday-mgt", 8082, true, 7, "Wallet", "Holiday management service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/salary-mgt", "薪资管理", "salary-mgt", 8092, true, 8, "Calculator", "Salary management service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/talent/talent-devep", "人才发展", "talent-assess", 8094, true, 9, "DataBoard", "Talent development service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/talent/talent-devep-prj", "人才发展项目", "talent-devep", 8095, true, 10, "FolderOpened", "Talent development project service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/talent/comp-assess", "能力评估", "talent-evaluation", 8090, true, 11, "Checked", "Competency assessment service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/hr-planning", "人力规划", "hr-planning", 8089, true, 12, "TrendingUp", "HR planning service"),
                new ModuleConfig("/ehrm", "人力资源管理", "/ehrm/skill-certification", "技能认证", "skill-certification", 8093, true, 13, "Medals", "Skill certification service"),

                new ModuleConfig("/strategy-mgt", "战略管理", "/strategy-mgt/strategy-planning", "战略规划", "strategy-planning", 8101, true, 1, "Trophy", "Strategy planning service"),
                new ModuleConfig("/strategy-mgt", "战略管理", "/strategy-mgt/strategy-exec", "战略执行", "strategy-exec", 8102, true, 2, "TrendingUp", "Strategy execution service"),
                new ModuleConfig("/strategy-mgt", "战略管理", "/strategy-mgt/work-plan-mgt", "工作计划管理", "work-plan-mgt", 8103, true, 3, "FileText", "Work plan management service"),

                new ModuleConfig("/org-mgt", "组织管理", "/org-mgt/org-tree", "五级组织管理", "org-mgt", 8074, true, 1, "Building", "Organization management service"),

                new ModuleConfig("/eehs", "环境健康安全", "/eehs/safety-mgt", "安全管理", "safety-mgt", 8121, true, 1, "Shield", "Safety management service"),
                new ModuleConfig("/eehs", "环境健康安全", "/eehs/environ-mgt", "环境管理", "enviro-mgt", 8122, true, 2, "Cloudy", "Environment management service"),
                new ModuleConfig("/eehs", "环境健康安全", "/eehs/ohm", "职业健康", "occu-health-mgt", 8123, true, 3, "FirstAidKit", "Occupational health service"),
                new ModuleConfig("/eehs", "环境健康安全", "/eehs/energy-mgt", "能源管理", "energy-mgt", 8124, true, 4, "Lightning", "Energy management service"),

                new ModuleConfig("/audit-project-improve", "审计项目与改善", "/audit-mgt/plan", "审计计划", "audit-mgt", 8078, true, 1, "DocumentChecked", "Audit management service"),
                new ModuleConfig("/audit-project-improve", "审计项目与改善", "/kaizen-pmt/prj-mgt", "项目管理", "project-mgt", 8076, true, 2, "FolderOpened", "Project management service"),
                new ModuleConfig("/audit-project-improve", "审计项目与改善", "/kaizen-pmt/kaizen-mgt", "改善管理", "improve-mgt", 8077, true, 3, "EditPen", "Improvement management service"),

                new ModuleConfig("/sys-mgt-mon", "系统管理监控", "/sys-mgt-mon/sys-mgt", "系统管理", "sys-mgt", 8111, true, 1, "Setting", "System management service"),
                new ModuleConfig("/sys-mgt-mon", "系统管理监控", "/sys-mgt-mon/sys-monitor", "系统监控", "sys-monitor", 8112, true, 2, "Monitor", "System monitoring service")
        );

        moduleConfigRepository.saveAll(defaults);
    }
}