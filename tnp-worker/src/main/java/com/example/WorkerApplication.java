package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@EnableRetry
@ComponentScan(basePackages = {"com.example"})
public class WorkerApplication {
    public static void main(String[] args) throws Exception {

        SpringApplication.run(WorkerApplication.class, args);
    }
}
