package com.example.discipline;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.discipline.**.mapper")
public class DisciplineMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(DisciplineMgtApplication.class, args);
    }
}
