package com.example.web.controller.secured.user;

import com.example.data.transactional.entities.Trade;
import com.example.data.transactional.view.TradeResult;
import com.example.security.JwtUtils;
import com.example.service.TradeService;
import com.example.web.utils.JsonApiSuccessUtil;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/user/trades")
@RequiredArgsConstructor
public class TradesController {

    private final TradeService tradeService;

    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<?> getUserTrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Trade.Type type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("Authorization") String authHeader) {

        Page<TradeResult> trades =
                tradeService.getUserTrades(
                        jwtUtils.extractUserId(jwtUtils.extractToken(authHeader)),
                        page,
                        size,
                        q,
                        type,
                        from,
                        to,
                        sortBy,
                        direction);

        return JsonApiSuccessUtil.createSuccessResponse(
                HttpStatus.OK, "Trades fetched successfully", trades);
    }
}
