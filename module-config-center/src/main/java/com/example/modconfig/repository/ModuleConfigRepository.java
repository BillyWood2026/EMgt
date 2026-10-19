package com.example.modconfig.repository;

import com.example.modconfig.entity.ModuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleConfigRepository extends JpaRepository<ModuleConfig, Long> {

    List<ModuleConfig> findByParentModuleKeyOrderByDisplayOrder(String parentModuleKey);

    List<ModuleConfig> findByParentModuleKeyAndEnabledTrueOrderByDisplayOrder(String parentModuleKey);

    List<ModuleConfig> findByEnabledTrueOrderByParentModuleKeyAscDisplayOrderAsc();

    Optional<ModuleConfig> findBySubModuleKey(String subModuleKey);

    boolean existsBySubModuleKey(String subModuleKey);

    List<ModuleConfig> findByParentModuleKey(String parentModuleKey);

    void deleteBySubModuleKey(String subModuleKey);
}