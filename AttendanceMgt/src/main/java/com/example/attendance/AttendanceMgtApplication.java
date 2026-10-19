package com.example.attendance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.attendance.mapper")
public class AttendanceMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceMgtApplication.class, args);
    }
}
