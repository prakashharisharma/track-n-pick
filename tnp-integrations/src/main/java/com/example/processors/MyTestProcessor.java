package com.example.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class MyTestProcessor implements Processor {

    public void process(Exchange exchange) throws Exception {
        System.out.println("Executing camel processor");
    }

}