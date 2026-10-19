package com.example.discipline.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 纪检模块 RBAC 注册：写操作（POST/PUT/DELETE/PATCH）需至少具备纪检管理角色或超管。
 * 角色常量：DISCIPLINE_LEAD 纪检主管；INSPECTOR 纪检监察员；DISCIPLINE_OFFICER 纪检干事；C99/ADMIN 超管。
 */
@Configuration
public class RbacWebConfig implements WebMvcConfigurer {

    public static final String DISCIPLINE_LEAD = "DISCIPLINE_LEAD";
    public static final String INSPECTOR = "INSPECTOR";
    public static final String DISCIPLINE_OFFICER = "DISCIPLINE_OFFICER";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RbacInterceptor("纪检", DISCIPLINE_LEAD, INSPECTOR, DISCIPLINE_OFFICER))
                .addPathPatterns("/api/discipline/**")
                .excludePathPatterns("/error", "/health", "/actuator/**");
    }
}
