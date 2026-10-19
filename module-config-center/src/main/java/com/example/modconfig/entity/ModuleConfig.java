package com.example.modconfig.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "module_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_module_key", nullable = false, length = 50)
    private String parentModuleKey;

    @Column(name = "parent_module_name", nullable = false, length = 100)
    private String parentModuleName;

    @Column(name = "sub_module_key", nullable = false, length = 100)
    private String subModuleKey;

    @Column(name = "sub_module_name", nullable = false, length = 100)
    private String subModuleName;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "service_port")
    private Integer servicePort;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ModuleConfig(String parentModuleKey, String parentModuleName, String subModuleKey, 
                       String subModuleName, String serviceName, Integer servicePort, 
                       Boolean enabled, Integer displayOrder, String icon, String description) {
        this.parentModuleKey = parentModuleKey;
        this.parentModuleName = parentModuleName;
        this.subModuleKey = subModuleKey;
        this.subModuleName = subModuleName;
        this.serviceName = serviceName;
        this.servicePort = servicePort;
        this.enabled = enabled;
        this.displayOrder = displayOrder;
        this.icon = icon;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}