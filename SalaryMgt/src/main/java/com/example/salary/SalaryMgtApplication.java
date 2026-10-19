package com.example.salary;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.salary.mapper")
public class SalaryMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalaryMgtApplication.class, args);
    }
}
