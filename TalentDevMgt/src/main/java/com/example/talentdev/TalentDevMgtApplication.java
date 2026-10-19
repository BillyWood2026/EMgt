package com.example.talentdev;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.talentdev.mapper")
public class TalentDevMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalentDevMgtApplication.class, args);
    }
}
