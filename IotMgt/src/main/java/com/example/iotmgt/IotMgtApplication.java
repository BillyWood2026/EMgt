package com.example.iotmgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.iotmgt.**.mapper")
public class IotMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(IotMgtApplication.class, args);
    }
}
