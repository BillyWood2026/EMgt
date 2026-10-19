package com.example.orgmgt;

import com.example.flowengine.sdk.config.FlowEngineAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@MapperScan("com.example.orgmgt.mapper")
@ComponentScan(basePackages = {"com.example.orgmgt", "com.example.common"})
@Import(FlowEngineAutoConfiguration.class)
public class OrgMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrgMgtApplication.class, args);
    }
}