package com.example.screenmgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.screenmgt.**.mapper")
public class ScreenMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScreenMgtApplication.class, args);
    }
}
