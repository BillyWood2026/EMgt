package com.example.skillcert;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.skillcert.mapper")
public class SkillCertMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillCertMgtApplication.class, args);
    }
}
