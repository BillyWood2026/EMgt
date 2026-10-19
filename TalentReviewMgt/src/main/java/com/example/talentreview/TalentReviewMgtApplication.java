package com.example.talentreview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.talentreview.mapper")
public class TalentReviewMgtApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalentReviewMgtApplication.class, args);
    }
}
