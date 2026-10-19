package com.example.environmgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.environmgt.**.mapper")
public class EnvironMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnvironMgtApplication.class, args);
    }
}
