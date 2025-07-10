package com.example.web.controller.secured.user;

import com.example.data.common.type.Timeframe;
import com.example.dto.common.OHLCV;
import com.example.service.OhlcvService;
import com.example.util.MiscUtil;
import com.example.web.utils.JsonApiSuccessUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("v1/user/ohlcvs")
public class OhlcvController {

    private final OhlcvService ohlcvService;
    private final MiscUtil miscUtil;

    @GetMapping
    public ResponseEntity<Map<String, Object>> fetchOHLCV(
            @RequestParam Timeframe timeframe, @RequestParam String symbol) {
        LocalDate to = miscUtil.currentDate();
        LocalDate from;

        switch (timeframe) {
            case DAILY -> from = to.minusYears(3);
            case WEEKLY -> from = to.minusYears(14);
            case MONTHLY -> from = to.minusYears(20);
            default -> throw new IllegalArgumentException("Unsupported timeframe: " + timeframe);
        }

        List<OHLCV> data = ohlcvService.fetch(timeframe, symbol, from, to);
        return JsonApiSuccessUtil.ok("OHLCV data retrieved successfully", data);
    }
}
