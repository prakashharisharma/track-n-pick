package com.example.web.controller.secured.admin;

import com.example.data.transactional.entities.SpecialTradingSession;
import com.example.service.SpecialTradingSessionService;
import com.example.web.utils.JsonApiErrorUtil;
import com.example.web.utils.JsonApiSuccessUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("admin/api/v1/special-trading-sessions")
@RestController
public class SpecialTradingSessionAdminController {

    private final SpecialTradingSessionService specialTradingSessionService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {


        Page<SpecialTradingSession> sessions = specialTradingSessionService.getAllSessions(page, size, from, to);
        return JsonApiSuccessUtil.ok("Special trading sessions retrieved successfully", Map.of(
                "items", sessions.getContent(),
                "currentPage", sessions.getNumber(),
                "totalPages", sessions.getTotalPages(),
                "totalItems", sessions.getTotalElements()
        ));
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody @Valid SpecialTradingSession session) {
        SpecialTradingSession createdSession = specialTradingSessionService.createSession(session);
        return JsonApiSuccessUtil.created("Session created successfully", createdSession);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSession(@PathVariable Long id, @RequestBody @Valid SpecialTradingSession session) {
        return specialTradingSessionService.updateSession(id, session)
                .map(updatedSession -> JsonApiSuccessUtil.updated("Session updated successfully", updatedSession))
                .orElse(JsonApiErrorUtil.createErrorResponse(HttpStatus.NOT_FOUND, "Session Not Found", "No session found with the given ID."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable Long id) {
        boolean deleted = specialTradingSessionService.deleteSession(id);
        if (deleted) {
            return JsonApiSuccessUtil.deleted("Session deleted successfully");
        } else {
            return JsonApiErrorUtil.createErrorResponse(HttpStatus.NOT_FOUND, "Session Not Found", "No session found with the given ID.");
        }
    }
}
