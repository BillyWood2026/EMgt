package com.example.occhealthmgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.occhealthmgt.**.mapper")
public class OccHealthMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(OccHealthMgtApplication.class, args);
    }
}
