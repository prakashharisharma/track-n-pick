package com.example.web.controller.secured.user;

import com.example.data.transactional.view.FundsLedgerResult;
import com.example.dto.request.FundsLedgerRequest;
import com.example.security.JwtUtils;
import com.example.service.FundsLedgerService;
import com.example.web.utils.JsonApiSuccessUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("v1/user/funds")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Funds", description = "Manage user fund transactions")
public class FundsController {

    private final FundsLedgerService fundsLedgerService;
    private final JwtUtils jwtUtils;

    @PostMapping("/add")
    public ResponseEntity<?> addFunds(@RequestBody @Valid FundsLedgerRequest request,
                                      @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtils.extractUserId(jwtUtils.extractToken(authHeader));
        fundsLedgerService.addFund(userId, request);
        return JsonApiSuccessUtil.createSuccessResponse(HttpStatus.OK, "Funds added successfully", request);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawFunds(@RequestBody @Valid FundsLedgerRequest request,
                                           @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtils.extractUserId(jwtUtils.extractToken(authHeader));
        fundsLedgerService.withdrawFund(userId, request);
        return JsonApiSuccessUtil.createSuccessResponse(HttpStatus.OK, "Funds withdrawn successfully", request);
    }

    @GetMapping
    public ResponseEntity<?> listFunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtils.extractUserId(jwtUtils.extractToken(authHeader));
        Page<FundsLedgerResult> result =
                fundsLedgerService.listFunds(userId, page, size, sortBy, direction);
        return JsonApiSuccessUtil.createSuccessResponse(
                HttpStatus.OK, "Funds transactions fetched", result);
    }
}
