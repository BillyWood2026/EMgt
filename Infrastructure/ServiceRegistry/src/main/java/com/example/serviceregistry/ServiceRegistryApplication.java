package com.example.serviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Nacos Service Registry Application
 * 
 * This is an OPTIONAL infrastructure module.
 * If not installed, services will run in standalone mode without service discovery.
 * 
 * To enable: Install this module and configure bootstrap.yml with nacos server address.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
