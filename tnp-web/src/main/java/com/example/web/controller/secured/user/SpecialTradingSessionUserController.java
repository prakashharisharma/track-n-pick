package com.example.web.controller.secured.user;

import com.example.service.SpecialTradingSessionService;
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

@RestController
@RequestMapping("api/v1/special-trading-sessions")
@RequiredArgsConstructor
public class SpecialTradingSessionUserController {

    private final SpecialTradingSessionService specialTradingSessionService;

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getSessionForToday() {
        LocalDate sessionDate = LocalDate.now();

        return specialTradingSessionService.getSessionByDate(sessionDate)
                .map(session -> JsonApiSuccessUtil.ok("Session retrieved successfully", session))
                .orElse(JsonApiErrorUtil.createErrorResponse(HttpStatus.NOT_FOUND, "Session Not Found", "No special trading session exists for today."));
    }
}
