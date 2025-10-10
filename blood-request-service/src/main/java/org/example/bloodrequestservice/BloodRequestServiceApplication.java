package org.example.bloodrequestservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class BloodRequestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BloodRequestServiceApplication.class, args);
    }

}
