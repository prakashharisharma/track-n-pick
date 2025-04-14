package com.example.web.controller.secured.user;

import com.example.data.transactional.view.PortfolioResult;
import com.example.dto.request.TradeRequest;
import com.example.security.JwtUtils;
import com.example.service.PortfolioService;
import com.example.web.utils.JsonApiSuccessUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/user/portfolio")
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

    @Operation(
            summary = "Get user portfolio",
            description =
                    "Fetches a paginated and sorted list of stocks in the user's portfolio with"
                            + " calculated P&L and other stock metrics",
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                @Parameter(
                        name = "page",
                        description = "Page number for pagination (0-based index)",
                        in = ParameterIn.QUERY,
                        schema = @Schema(type = "integer", defaultValue = "0")),
                @Parameter(
                        name = "size",
                        description = "Number of records per page",
                        in = ParameterIn.QUERY,
                        schema = @Schema(type = "integer", defaultValue = "10")),
                @Parameter(
                        name = "sortBy",
                        description =
                                "Field to sort by (symbol, name, changePercent, quantity,"
                                        + " investment, currentValue, pnlPercent)",
                        in = ParameterIn.QUERY,
                        schema = @Schema(type = "string", defaultValue = "symbol")),
                @Parameter(
                        name = "direction",
                        description = "Sort direction (asc or desc)",
                        in = ParameterIn.QUERY,
                        schema = @Schema(type = "string", defaultValue = "asc")),
                @Parameter(
                        name = "Authorization",
                        description = "Bearer token for authenticated user",
                        in = ParameterIn.HEADER,
                        required = true,
                        schema = @Schema(type = "string"))
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User portfolio fetched successfully"),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized – invalid or missing token"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @GetMapping
    public ResponseEntity<?> getUserPortfolio(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "symbol") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestHeader("Authorization") String authHeader) {

        Page<PortfolioResult> portfolio =
                portfolioService.get(
                        jwtUtils.extractUserId(jwtUtils.extractToken(authHeader)),
                        page,
                        size,
                        sortBy,
                        direction);
        return JsonApiSuccessUtil.createSuccessResponse(
                HttpStatus.OK, "User portfolio fetched successfully", portfolio);
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Get user portfolio stats",
            description =
                    "Returns total investment, current value, and profit/loss percentage for the"
                            + " user's portfolio",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                @ApiResponse(responseCode = "200", description = "Stats fetched successfully"),
                @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    public ResponseEntity<Map<String, Object>> getUserPortfolioStats(
            @RequestHeader("Authorization") String authHeader) {

        PortfolioResult stats =
                portfolioService.stats(jwtUtils.extractUserId(jwtUtils.extractToken(authHeader)));

        return JsonApiSuccessUtil.createSuccessResponse(
                HttpStatus.OK, "User portfolio stats fetched successfully", stats);
    }
}
