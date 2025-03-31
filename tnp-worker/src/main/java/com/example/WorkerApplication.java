package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableScheduling
@EnableRetry
@ComponentScan(basePackages = {"com.example"})
@EnableJpaRepositories(basePackages = {"com.example.data.transactional.repo"})
@EntityScan(basePackages = {"com.example.data.transactional.entities"})
@EnableMongoRepositories(basePackages = {"com.example.data.storage.repo"})
public class WorkerApplication {
    public static void main(String[] args) throws Exception {

        SpringApplication.run(WorkerApplication.class, args);
    }
}
