package com.example.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import com.example.mq.producer.QueueService;




//@Component
public class AppRunner1 implements CommandLineRunner {
	private static Logger log = LoggerFactory.getLogger(AppRunner1.class);

    @Autowired
    private QueueService orderService;

    @Override
    public void run(String... strings) throws Exception {

        log.info("Spring Boot Embedded ActiveMQ Configuration Example");

        for (int i = 0; i < 5; i++){
            int priority=i+1;
           // Order order = new Order(i,  " - Sending JMS Message using Embedded activeMQ", LocalDateTime.now());
            
            String test = "This is a test";
            
            orderService.send(test, "test-queue");

        }

        log.info("Waiting for all ActiveMQ JMS Messages to be consumed");
        //TimeUnit.SECONDS.sleep(3);
        //System.exit(-1);
    }

}
