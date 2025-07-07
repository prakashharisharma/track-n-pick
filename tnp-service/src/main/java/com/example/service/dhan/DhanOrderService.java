package com.example.service.dhan;

import com.example.data.transactional.entities.DhanOrder;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.repo.DhanOrderRepository;
import com.example.external.dhan.model.OrderRequest;
import com.example.external.dhan.model.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DhanOrderService {

    private final DhanOrderRepository dhanOrderRepository;

    @Transactional
    public DhanOrder saveOrder(
            OrderRequest request, OrderResponse response, User user, Stock stock) {
        DhanOrder order =
                DhanOrder.builder()
                        .user(user)
                        .dhanClientId(user.getDhanClientId())
                        .orderId(response.getOrderId())
                        .tradingSymbol(stock.getNseSymbol())
                        .stock(stock)
                        .securityId(request.getSecurityId())
                        .quantity(Long.parseLong(request.getQuantity()))
                        .disclosedQuantity(Long.parseLong(request.getDisclosedQuantity()))
                        .price(Double.parseDouble(request.getPrice()))
                        .transactionType(request.getTransactionType().name())
                        .orderType(request.getOrderType().name())
                        .afterMarketOrder(request.getAfterMarketOrder())
                        .exchangeSegment(request.getExchangeSegment())
                        .productType(request.getProductType().name())
                        .validity(request.getValidity().name())
                        .status(response.getStatus().name())
                        .build();

        log.info(
                "Saving order details for orderId: {}, userId: {}",
                response.getOrderId(),
                user.getId());
        return dhanOrderRepository.save(order);
    }
}
