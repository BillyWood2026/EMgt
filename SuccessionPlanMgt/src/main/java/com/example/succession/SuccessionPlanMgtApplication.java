package com.example.succession;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.succession.mapper")
public class SuccessionPlanMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuccessionPlanMgtApplication.class, args);
    }
}
