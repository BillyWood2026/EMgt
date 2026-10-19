package com.example.training;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.training.mapper")
public class TrainingMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingMgtApplication.class, args);
    }
}
