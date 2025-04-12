package com.example.web.controller.secured.user;

import com.example.dto.request.TradeRequest;
import com.example.security.JwtUtils;
import com.example.service.PortfolioService;
import com.example.web.utils.JsonApiSuccessUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Tag(
        name = "Portfolio",
        description = "Operations related to buying and selling stocks in user's portfolio")
@SecurityRequirement(name = "bearerAuth") // 🔐 Apply to all methods in this controller
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final JwtUtils jwtUtils;

    @Operation(
            summary = "Buy stock",
            description = "Allows a user to buy a specified quantity of stock at a given price",
            responses = {
                @ApiResponse(responseCode = "200", description = "Stock bought successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid request"),
                @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    @PostMapping("/buy")
    public ResponseEntity<Map<String, Object>> buyStock(
            @Valid @RequestBody TradeRequest request,
            @RequestHeader("Authorization") String authHeader) {

        portfolioService.buyStock(
                jwtUtils.extractUserId(jwtUtils.extractToken(authHeader)),
                request.getStockId(),
                request.getQuantity(),
                request.getPrice());
        return JsonApiSuccessUtil.createSuccessResponse(
                HttpStatus.OK, "Stock bought successfully", request);
    }

    @Operation(
            summary = "Sell stock",
            description = "Allows a user to sell a specified quantity of stock at a given price",
            responses = {
                @ApiResponse(responseCode = "200", description = "Stock sold successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid request"),
                @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    @PostMapping("/sell")
    public ResponseEntity<Map<String, Object>> sellStock(
            @Valid @RequestBody TradeRequest request,
            @RequestHeader("Authorization") String authHeader) {

        portfolioService.sellStock(
                jwtUtils.extractUserId(jwtUtils.extractToken(authHeader)),
                request.getStockId(),
                request.getQuantity(),
                request.getPrice());
        return JsonApiSuccessUtil.createSuccessResponse(
                HttpStatus.OK, "Stock sold successfully", request);
    }
}
