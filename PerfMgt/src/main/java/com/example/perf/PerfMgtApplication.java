package com.example.perf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.perf.mapper")
public class PerfMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(PerfMgtApplication.class, args);
    }
}
