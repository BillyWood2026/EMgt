package com.example.datamgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.datamgt.**.mapper")
public class DataMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataMgtApplication.class, args);
    }
}
