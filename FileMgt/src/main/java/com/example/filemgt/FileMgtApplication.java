package com.example.filemgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.filemgt.**.mapper")
public class FileMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileMgtApplication.class, args);
    }
}
