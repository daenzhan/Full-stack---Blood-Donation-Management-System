package org.example.medcenterservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MedCenterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedCenterServiceApplication.class, args);
    }

}
