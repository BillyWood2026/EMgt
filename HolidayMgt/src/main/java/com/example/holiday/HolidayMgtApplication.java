package com.example.holiday;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.holiday.mapper")
public class HolidayMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(HolidayMgtApplication.class, args);
    }
}
