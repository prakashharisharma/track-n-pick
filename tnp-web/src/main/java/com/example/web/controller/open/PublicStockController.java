package com.example.web.controller.open;

import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import com.example.web.utils.JsonApiSuccessUtil;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/api/stocks")
public class PublicStockController {

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchStocks(
            @RequestParam Optional<String> query,
            @RequestParam Optional<Long> sectorId,
            @RequestParam(defaultValue = "nseSymbol") String sortBy,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        return JsonApiSuccessUtil.createSuccessResponse(
                HttpStatus.OK, "Results", new ArrayList<>());
    }
}
