package com.example.onlineexam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.onlineexam.mapper")
public class OnlineExamMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineExamMgtApplication.class, args);
    }
}
