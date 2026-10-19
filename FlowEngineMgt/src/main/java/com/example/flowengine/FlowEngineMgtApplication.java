package com.example.flowengine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.flowengine.**.mapper")
public class FlowEngineMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlowEngineMgtApplication.class, args);
    }
}
