package com.example.web.controller.secured.user;

import com.example.data.transactional.entities.SpecialTradingSession;
import com.example.service.SpecialTradingSessionService;
import com.example.web.utils.JsonApiSuccessUtil;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/user/special-trading-sessions")
@RequiredArgsConstructor
public class SpecialTradingSessionUserController {

    private final SpecialTradingSessionService specialTradingSessionService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSessionForToday() {

        List<SpecialTradingSession> data =
                specialTradingSessionService.getUpcomingSpecialSessions();

        return JsonApiSuccessUtil.ok("Session retrieved successfully", data);
    }
}
