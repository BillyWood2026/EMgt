package com.example.holiday.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 假期管理模块 RBAC 注册：写操作（POST/PUT/DELETE/PATCH）需至少具备 HR 管理角色或超管。
 * 角色常量：HR_ADMIN 人力管理员；HR_MANAGER 人力经理；DEPT_HR 部门 HR；C99/ADMIN 超管。
 */
@Configuration
public class RbacWebConfig implements WebMvcConfigurer {

    public static final String HR_ADMIN = "HR_ADMIN";
    public static final String HR_MANAGER = "HR_MANAGER";
    public static final String DEPT_HR = "DEPT_HR";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RbacInterceptor("假期管理", HR_ADMIN, HR_MANAGER, DEPT_HR))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/error", "/health", "/actuator/**");
    }
}