package com.example.processmgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.processmgt.mapper")
public class ProcessMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcessMgtApplication.class, args);
    }
}