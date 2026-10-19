package com.example.modconfig.controller;

import com.example.modconfig.entity.ModuleConfig;
import com.example.modconfig.service.ModuleConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/module-config")
@RequiredArgsConstructor
public class ModuleConfigController {

    private final ModuleConfigService moduleConfigService;

    @GetMapping
    public ResponseEntity<List<ModuleConfig>> getAllModules() {
        return ResponseEntity.ok(moduleConfigService.getAllModules());
    }

    @GetMapping("/enabled")
    public ResponseEntity<List<ModuleConfig>> getEnabledModules() {
        return ResponseEntity.ok(moduleConfigService.getEnabledModules());
    }

    @GetMapping("/grouped")
    public ResponseEntity<Map<String, List<ModuleConfig>>> getModulesGroupedByParent() {
        return ResponseEntity.ok(moduleConfigService.getModulesGroupedByParent());
    }

    @GetMapping("/parent/{parentKey}")
    public ResponseEntity<List<ModuleConfig>> getModulesByParent(@PathVariable String parentKey) {
        return ResponseEntity.ok(moduleConfigService.getModulesByParent(parentKey));
    }

    @GetMapping("/nav-config")
    public ResponseEntity<Map<String, Object>> getNavigationConfig() {
        return ResponseEntity.ok(moduleConfigService.getModuleNavigationConfig());
    }

    @GetMapping("/sub/{subKey}")
    public ResponseEntity<ModuleConfig> getModuleBySubKey(@PathVariable String subKey) {
        return moduleConfigService.getModuleBySubKey(subKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ModuleConfig> createModule(@RequestBody ModuleConfig config) {
        return ResponseEntity.ok(moduleConfigService.createModule(config));
    }

    @PutMapping("/sub/{subKey}")
    public ResponseEntity<ModuleConfig> updateModule(
            @PathVariable String subKey, 
            @RequestBody ModuleConfig config) {
        return ResponseEntity.ok(moduleConfigService.updateModule(subKey, config));
    }

    @PatchMapping("/sub/{subKey}/toggle")
    public ResponseEntity<Map<String, Object>> toggleSubModule(@PathVariable String subKey) {
        ModuleConfig config = moduleConfigService.getModuleBySubKey(subKey).orElse(null);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        config.setEnabled(!config.getEnabled());
        moduleConfigService.save(config);
        Map<String, Object> result = new HashMap<>();
        result.put("subModuleKey", subKey);
        result.put("enabled", config.getEnabled());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/sub/{subKey}")
    public ResponseEntity<Void> deleteModule(@PathVariable String subKey) {
        moduleConfigService.deleteModule(subKey);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initialize")
    public ResponseEntity<Void> initializeDefaults() {
        moduleConfigService.initializeDefaultConfigs();
        return ResponseEntity.ok().build();
    }
}