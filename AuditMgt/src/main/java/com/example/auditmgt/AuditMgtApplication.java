package com.example.auditmgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.auditmgt.mapper")
public class AuditMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditMgtApplication.class, args);
    }
}
