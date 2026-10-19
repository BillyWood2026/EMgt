package com.example.partyaffairsmgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.partyaffairsmgt")
public class PartyAffairsMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartyAffairsMgtApplication.class, args);
    }
}
