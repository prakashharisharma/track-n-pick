package com.example.web.controller.secured.user;

import com.example.data.transactional.entities.TradingHoliday;
import com.example.service.TradingHolidayService;
import com.example.web.utils.JsonApiSuccessUtil;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("v1/user/trading-holidays")
@RestController
public class TradingHolidayUserController {

    private final TradingHolidayService tradingHolidayService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTodayHoliday() {

        List<TradingHoliday> data = tradingHolidayService.getUpcomingHolidays();

        return JsonApiSuccessUtil.ok("Holidays retrieved successfully", data);
    }
}
