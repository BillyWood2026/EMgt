package com.example.safetymgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.safetymgt.**.mapper")
public class SafetyMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(SafetyMgtApplication.class, args);
    }
}
