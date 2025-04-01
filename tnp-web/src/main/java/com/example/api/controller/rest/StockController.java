package com.example.api.controller.rest;

import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.repo.StockRepository;
import com.example.service.StockService;
import com.example.util.FormulaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @Autowired private StockRepository stockRepository;

    @Autowired private StockService stockService;

    @Autowired private FormulaService formulaService;

    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Stock>> getStocksMaster() {

        return ResponseEntity.ok(stockRepository.findAll());
    }
}
