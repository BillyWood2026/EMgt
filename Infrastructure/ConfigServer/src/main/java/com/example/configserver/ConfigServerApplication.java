package com.example.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Nacos Config Server Application
 * 
 * This is an OPTIONAL infrastructure module.
 * If not installed, services will use local configuration files.
 * 
 * This module acts as a bridge to Nacos for externalized configuration.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
