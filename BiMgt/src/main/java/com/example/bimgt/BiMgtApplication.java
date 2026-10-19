package com.example.bimgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.bimgt.**.mapper")
public class BiMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(BiMgtApplication.class, args);
    }
}
