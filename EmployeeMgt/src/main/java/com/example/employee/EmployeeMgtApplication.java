package com.example.employee;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.employee.mapper")
public class EmployeeMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeMgtApplication.class, args);
    }
}
