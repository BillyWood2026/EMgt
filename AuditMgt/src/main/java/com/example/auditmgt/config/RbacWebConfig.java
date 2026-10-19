package com.example.auditmgt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 审计模块 RBAC 注册：写操作（POST/PUT/DELETE/PATCH）需至少具备审计管理角色或超管。
 * 角色常量：AUDIT_LEAD 审计主管；AUDIT_MANAGER 审计管理员；CFO 财务总监；C99/ADMIN 超管。
 */
@Configuration
public class RbacWebConfig implements WebMvcConfigurer {

    public static final String AUDIT_LEAD = "AUDIT_LEAD";
    public static final String AUDIT_MANAGER = "AUDIT_MANAGER";
    public static final String CFO = "CFO";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RbacInterceptor("审计", AUDIT_LEAD, AUDIT_MANAGER, CFO))
                .addPathPatterns("/api/audit/**")
                .excludePathPatterns("/error", "/health", "/actuator/**");
    }
}
