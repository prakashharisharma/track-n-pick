package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.example"})
public class WorkerApplication {
    public static void main(String[] args) throws Exception {

        SpringApplication.run(WorkerApplication.class, args);
    }
}
