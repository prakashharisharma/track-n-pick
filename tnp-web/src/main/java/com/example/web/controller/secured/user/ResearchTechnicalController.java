package com.example.web.controller.secured.user;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Trade;
import com.example.data.transactional.view.ResearchTechnicalResult;
import com.example.security.JwtUtils;
import com.example.service.ResearchTechnicalService;
import com.example.web.utils.JsonApiSuccessUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/user/research-technicals")
@RequiredArgsConstructor
public class ResearchTechnicalController {

    private final ResearchTechnicalService researchTechnicalService;
    private final JwtUtils jwtUtils;

    @GetMapping("/history")
    public Page<ResearchTechnicalResult> searchHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Trade.Type type,
            @RequestParam(required = false) Timeframe timeframe,
            @RequestParam(defaultValue = "researchDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return researchTechnicalService.searchHistory(
                page, size, type, timeframe, sortBy, direction);
    }

    @GetMapping("/history/{researchId}/details")
    public ResponseEntity<Map<String, Object>> historyDetails(
            @PathVariable long researchId, @RequestHeader("Authorization") String authHeader) {
        return JsonApiSuccessUtil.createSuccessResponse(
                HttpStatus.OK,
                "Stock details retrieved successfully",
                researchTechnicalService.getHistoryDetails(researchId));
    }

    @GetMapping("/current")
    public Page<ResearchTechnicalResult> searchCurrent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Trade.Type type,
            @RequestParam(required = false) Timeframe timeframe,
            @RequestParam(defaultValue = "researchDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return researchTechnicalService.searchCurrent(
                page, size, type, timeframe, sortBy, direction);
    }

    @GetMapping("/current/{researchId}/details")
    public ResponseEntity<Map<String, Object>> currentDetails(
            @PathVariable long researchId, @RequestHeader("Authorization") String authHeader) {
        return JsonApiSuccessUtil.createSuccessResponse(
                HttpStatus.OK,
                "Stock details retrieved successfully",
                researchTechnicalService.getCurrentDetails(
                        jwtUtils.extractUserId(jwtUtils.extractToken(authHeader)), researchId));
    }
}
