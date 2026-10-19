package com.example.energymgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.energymgt.**.mapper")
public class EnergyMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnergyMgtApplication.class, args);
    }
}
