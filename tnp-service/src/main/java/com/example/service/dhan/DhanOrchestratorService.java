package com.example.service.dhan;

import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.entities.type.dhan.*;
import com.example.external.dhan.DhanIntegrationService;
import com.example.external.dhan.model.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DhanOrchestratorService {

    private final DhanIntegrationService dhanIntegrationService;
    private final DhanOrderService dhanOrderService;

    private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(9, 15);
    public static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(15, 30);
    private static final LocalTime PRE_OPEN_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime PRE_OPEN_END_TIME = LocalTime.of(9, 7);
    private static final double DISCLOSED_QUANTITY_PERCENTAGE = 0.31;

    private boolean isBeforeMarketOpen() {
        return LocalTime.now().isBefore(MARKET_OPEN_TIME);
    }

    private boolean isAfterMarketClose() {
        return LocalTime.now().isAfter(MARKET_CLOSE_TIME);
    }

    private boolean isPreOpenMarket() {
        LocalTime now = LocalTime.now();
        return now.isAfter(PRE_OPEN_START_TIME) && now.isBefore(PRE_OPEN_END_TIME);
    }

    private boolean isAfterMarketOrder() {
        return (isBeforeMarketOpen() || isAfterMarketClose()) && !isPreOpenMarket();
    }

    private long calculateDisclosedQuantity(long totalQuantity) {
        return (long) (totalQuantity * DISCLOSED_QUANTITY_PERCENTAGE);
    }

    public List<Holding> getHoldings(String accessToken) {
        ResponseEntity<List<Holding>> response = dhanIntegrationService.getHoldings(accessToken);
        return response.getStatusCode() == HttpStatus.OK ? response.getBody() : new ArrayList<>();
    }

    public FundLimit getFundLimit(String accessToken) {
        ResponseEntity<FundLimit> response = dhanIntegrationService.getFundLimit(accessToken);
        return response.getBody();
    }

    public OrderResponse placeOrder(String accessToken, OrderRequest request) {
        ResponseEntity<OrderResponse> response =
                dhanIntegrationService.placeOrder(accessToken, request);
        return response.getBody();
    }

    public List<Order> getOrders(String accessToken) {
        ResponseEntity<List<Order>> response = dhanIntegrationService.getOrders(accessToken);
        return response.getBody();
    }

    public Order getOrder(String accessToken, String orderId) {
        ResponseEntity<Order> response = dhanIntegrationService.getOrder(accessToken, orderId);
        return response.getBody();
    }

    /**
     * Calculates the total net worth of a user's Dhan portfolio. Net worth is calculated as sum of
     * (totalQty * avgCostPrice) for all holdings.
     *
     * @param user the user whose net worth needs to be calculated
     * @return the total net worth, or 0.0 if user has no Dhan integration or holdings
     */
    public double calculateNetWorth(User user) {
        if (!user.isDhanApiEnabled()) {
            log.warn("User {} has no Dhan API integration enabled", user.getId());
            return 0.0;
        }

        if (user.getDhanAccessToken() == null || user.getDhanAccessToken().isEmpty()) {
            log.warn("User {} has no Dhan access token", user.getId());
            return 0.0;
        }

        List<Holding> holdings = getHoldings(user.getDhanAccessToken());
        if (holdings == null || holdings.isEmpty()) {
            return 0.0;
        }

        double holdingValue =
                holdings.stream()
                        .mapToDouble(holding -> holding.getTotalQty() * holding.getAvgCostPrice())
                        .sum();

        double fundLimit = this.getFundLimit(user);

        return holdingValue + fundLimit;
    }

    /**
     * Gets the available fund balance from user's Dhan account.
     *
     * @param user the user whose fund balance needs to be retrieved
     * @return the available balance, or 0.0 if user has no Dhan integration or fund limit info
     */
    public double getFundLimit(User user) {
        if (!user.isDhanApiEnabled()) {
            log.warn("User {} has no Dhan API integration enabled", user.getId());
            return 0.0;
        }

        if (user.getDhanAccessToken() == null || user.getDhanAccessToken().isEmpty()) {
            log.warn("User {} has no Dhan access token", user.getId());
            return 0.0;
        }

        FundLimit fundLimit = getFundLimit(user.getDhanAccessToken());
        if (fundLimit == null) {
            return 0.0;
        }

        return fundLimit.getAvailabelBalance() != null ? fundLimit.getAvailabelBalance() : 0.0;
    }

    /**
     * Places a buy order for a stock using Dhan's trading platform.
     *
     * @param user the user placing the order
     * @param stock the stock to buy
     * @param quantity number of shares to buy
     * @param price limit price for the order (null for market order)
     * @return the order response containing orderId and status
     * @throws IllegalStateException if user's Dhan integration is not enabled or token is missing
     */
    public OrderResponse placeOrder(
            TransactionType transactionType, User user, Stock stock, long quantity, double price) {

        if (!user.isDhanApiEnabled()) {
            throw new IllegalStateException(
                    "Dhan API integration is not enabled for user: " + user.getId());
        }

        if (user.getDhanAccessToken() == null || user.getDhanAccessToken().isEmpty()) {
            throw new IllegalStateException(
                    "Dhan access token is missing for user: " + user.getId());
        }

        boolean isAfterMarketOrder = isAfterMarketOrder();
        boolean isPreOpenMarket = isPreOpenMarket();
        long disclosedQuantity =
                isAfterMarketOrder || isPreOpenMarket ? 0 : calculateDisclosedQuantity(quantity);

        OrderRequest orderRequest =
                OrderRequest.builder()
                        .dhanClientId(user.getDhanClientId())
                        .securityId(stock.getInstrument())
                        .quantity(String.valueOf(quantity))
                        .price(String.valueOf(price))
                        .transactionType(transactionType)
                        .exchangeSegment("NSE_EQ")
                        .productType(ProductType.CNC)
                        .orderType(OrderType.LIMIT)
                        .validity(Validity.DAY)
                        .afterMarketOrder(isAfterMarketOrder)
                        .amoTime(isAfterMarketOrder ? AmoTime.PRE_OPEN : AmoTime.OPEN)
                        .build();

        if (disclosedQuantity > 0 && disclosedQuantity < quantity) {
            orderRequest.setDisclosedQuantity(String.valueOf(disclosedQuantity));
        }

        System.out.println(orderRequest);

        log.info(
                "Placing buy order for user: {}, stock: {}, quantity: {}, price: {}",
                user.getId(),
                stock.getNseSymbol(),
                quantity,
                price);

        OrderResponse response = placeOrder(user.getDhanAccessToken(), orderRequest);

        dhanOrderService.saveOrder(orderRequest, response, user, stock);

        return response;
    }

    public List<Holding> getHoldings(User user) {
        if (!user.isDhanApiEnabled()) {
            throw new IllegalStateException(
                    "Dhan API integration is not enabled for user: " + user.getId());
        }

        if (user.getDhanAccessToken() == null || user.getDhanAccessToken().isEmpty()) {
            throw new IllegalStateException(
                    "Dhan access token is missing for user: " + user.getId());
        }
        ResponseEntity<List<Holding>> response =
                dhanIntegrationService.getHoldings(user.getDhanAccessToken());
        return response.getBody();
    }

    public List<Trade> getTrades(User user) {
        if (!user.isDhanApiEnabled()) {
            throw new IllegalStateException(
                    "Dhan API integration is not enabled for user: " + user.getId());
        }

        if (user.getDhanAccessToken() == null || user.getDhanAccessToken().isEmpty()) {
            throw new IllegalStateException(
                    "Dhan access token is missing for user: " + user.getId());
        }
        ResponseEntity<List<Trade>> response =
                dhanIntegrationService.getTrades(user.getDhanAccessToken());
        return response.getBody();
    }
}
