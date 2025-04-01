package com.example.web.controller.secured.admin;

import com.example.data.transactional.entities.TradingHoliday;
import com.example.service.TradingHolidayService;
import com.example.web.utils.JsonApiErrorUtil;
import com.example.web.utils.JsonApiSuccessUtil;
import java.time.LocalDate;
import java.util.Map;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("admin/api/v1/trading-holidays")
@RestController
public class TradingHolidayAdminController {

    private final TradingHolidayService tradingHolidayService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllHolidays(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to) {

        Page<TradingHoliday> holidays = tradingHolidayService.getAllHolidays(page, size, from, to);
        return JsonApiSuccessUtil.ok(
                "Trading holidays retrieved successfully",
                Map.of(
                        "items", holidays.getContent(),
                        "currentPage", holidays.getNumber(),
                        "totalPages", holidays.getTotalPages(),
                        "totalItems", holidays.getTotalElements()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createHoliday(
            @RequestBody @Valid TradingHoliday holiday) {
        TradingHoliday createdHoliday = tradingHolidayService.createHoliday(holiday);
        return JsonApiSuccessUtil.created("Holiday created successfully", createdHoliday);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateHoliday(
            @PathVariable Long id, @RequestBody @Valid TradingHoliday holiday) {
        return tradingHolidayService
                .updateHoliday(id, holiday)
                .map(updated -> JsonApiSuccessUtil.updated("Holiday updated successfully", updated))
                .orElse(
                        JsonApiErrorUtil.createErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Holiday Not Found",
                                "No holiday found with the given ID."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteHoliday(@PathVariable Long id) {
        boolean deleted = tradingHolidayService.deleteHoliday(id);
        if (deleted) {
            return JsonApiSuccessUtil.deleted("Holiday deleted successfully");
        } else {
            return JsonApiErrorUtil.createErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Holiday Not Found",
                    "No holiday found with the given ID.");
        }
    }
}
