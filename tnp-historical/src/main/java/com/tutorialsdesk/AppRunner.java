package com.tutorialsdesk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.storage.service.StorageService;




//@Component
public class AppRunner implements CommandLineRunner {

    @Autowired
    private StorageService stockService;

    @Override
    public void run(String... strings) throws Exception {/*
       // studentService.deleteAll();
        // save a couple of customers
        stockService.saveStock(new Stock("1235"));
        stockService.saveStock(new Stock("1245"));
        stockService.saveStock(new Stock("123"));
        stockService.saveStock(new Stock("124"));
        // fetch all customers
        System.out.println("Students found with findAll():");
        System.out.println("-------------------------------");
        for (Stock stock : stockService.findAll()) {
            System.out.println(stock);
            
        }

        System.out.println();

        // fetch an individual customer
        System.out.println("Student found with findByName('123'):");
        System.out.println("--------------------------------");
        Stock stock = stockService.findByNseSymbol("123");
        stock.getStockPrices().add(new StockPrice(25.0));
        stockService.saveStock(stock);
       
        
        stock = stockService.findByNseSymbol("123");
        
        stock.getStockPrices().add(new StockPrice(35.0));
        stockService.saveStock(stock);
        stock = stockService.findByNseSymbol("123");
        System.out.println(stock);

     
    */}
}
