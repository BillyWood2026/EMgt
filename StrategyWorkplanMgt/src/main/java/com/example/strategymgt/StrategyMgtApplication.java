package com.example.strategymgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.strategymgt.**.mapper")
public class StrategyMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(StrategyMgtApplication.class, args);
    }
}
