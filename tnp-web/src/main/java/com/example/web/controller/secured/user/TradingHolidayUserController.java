package com.example.web.controller.secured.user;


import com.example.service.TradingHolidayService;
import com.example.web.utils.JsonApiErrorUtil;
import com.example.web.utils.JsonApiSuccessUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("api/v1/trading-holidays")
@RestController
public class TradingHolidayUserController {

    private final TradingHolidayService tradingHolidayService;

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayHoliday() {
        LocalDate today = LocalDate.now();
        return tradingHolidayService.getHolidayByDate(today)
                .map(holiday -> JsonApiSuccessUtil.ok("Today's trading holiday", holiday))
                .orElse(JsonApiErrorUtil.createErrorResponse(HttpStatus.NOT_FOUND, "No Holiday", "Today is not a trading holiday."));
    }
}