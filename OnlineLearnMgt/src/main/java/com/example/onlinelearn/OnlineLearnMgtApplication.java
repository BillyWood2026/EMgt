package com.example.onlinelearn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.onlinelearn.mapper")
public class OnlineLearnMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineLearnMgtApplication.class, args);
    }
}
