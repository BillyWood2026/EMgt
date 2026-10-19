package com.example.messagemgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.messagemgt.**.mapper")
public class MessageMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageMgtApplication.class, args);
    }
}
