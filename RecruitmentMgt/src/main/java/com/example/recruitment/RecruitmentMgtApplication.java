package com.example.recruitment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.recruitment.mapper")
public class RecruitmentMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecruitmentMgtApplication.class, args);
    }
}
