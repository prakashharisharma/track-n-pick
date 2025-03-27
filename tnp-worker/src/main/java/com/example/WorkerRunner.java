package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class WorkerRunner implements CommandLineRunner {

    @Override
    public void run(String... arg0) throws InterruptedException, IOException {
        log.info("Worker started....");
    }
}
