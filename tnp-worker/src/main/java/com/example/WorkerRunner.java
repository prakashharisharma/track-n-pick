package com.example;

import com.example.processor.BhavProcessor;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkerRunner implements CommandLineRunner {

    @Autowired private BhavProcessor bhavProcessor;

    @Override
    public void run(String... arg0) throws InterruptedException, IOException {
        log.info("Worker started....");
        // bhavProcessor.processAndResearchTechnicals();
    }
}
