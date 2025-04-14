package com.example.web.controller.secured.user;

import com.example.service.StockService;
import com.example.web.utils.JsonApiSuccessUtil;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/user/stocks")
public class StockController {

    private final StockService stockService;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchStocks(
            @RequestParam("q") String query) {

        return JsonApiSuccessUtil.createSuccessResponse(
                HttpStatus.OK, "Results", stockService.searchStocks(query));
    }
}
