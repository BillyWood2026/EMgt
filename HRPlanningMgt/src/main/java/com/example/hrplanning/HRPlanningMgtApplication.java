package com.example.hrplanning;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.hrplanning.mapper")
public class HRPlanningMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(HRPlanningMgtApplication.class, args);
    }
}
