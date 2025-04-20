package com.example.web.controller.secured.user;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Trade;
import com.example.data.transactional.view.ResearchTechnicalResult;
import com.example.service.ResearchTechnicalService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/user/research-technicals")
@RequiredArgsConstructor
public class ResearchTechnicalController {

    private final ResearchTechnicalService researchTechnicalService;

    @GetMapping("/history")
    public Page<ResearchTechnicalResult> searchHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Trade.Type type,
            @RequestParam(required = false) Timeframe timeframe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date,
            @RequestParam(defaultValue = "researchDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return researchTechnicalService.searchHistory(
                page, size, type, timeframe, date, sortBy, direction);
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
}
