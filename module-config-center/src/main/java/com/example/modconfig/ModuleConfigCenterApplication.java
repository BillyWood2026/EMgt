package com.example.modconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ModuleConfigCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleConfigCenterApplication.class, args);
    }
}