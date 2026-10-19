package com.example.talent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.talent.mapper")
public class TalentMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalentMgtApplication.class, args);
    }
}
