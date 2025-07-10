package com.example.service.dhan;

import com.example.data.transactional.entities.DhanTrade;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.repo.DhanTradeRepository;
import com.example.external.dhan.model.Trade;
import com.example.service.StockService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DhanTradeService {

    private final DhanTradeRepository dhanTradeRepository;
    private final StockService stockService;
    private final DhanOrchestratorService dhanOrchestratorService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public boolean processTrade(Trade trade, User user) {
        // Check if trade already exists
        Optional<DhanTrade> existingTrade =
                dhanTradeRepository.findByExchangeTradeId(trade.getExchangeTradeId());

        if (existingTrade.isPresent()) {
            log.debug("Trade already exists with exchangeTradeId: {}", trade.getExchangeTradeId());
            return false;
        }

        // Log trade details
        log.info(
                "Processing new BUY trade - Symbol: {}, Quantity: {}, Price: {}",
                trade.getTradingSymbol(),
                trade.getTradedQuantity(),
                trade.getTradedPrice());

        DhanTrade dhanTrade =
                DhanTrade.builder()
                        .user(user)
                        .dhanClientId(trade.getDhanClientId())
                        .orderId(trade.getOrderId())
                        .exchangeOrderId(trade.getExchangeOrderId())
                        .exchangeTradeId(trade.getExchangeTradeId())
                        .transactionType(trade.getTransactionType())
                        .exchangeSegment(trade.getExchangeSegment())
                        .productType(trade.getProductType())
                        .orderType(trade.getOrderType())
                        .tradingSymbol(trade.getTradingSymbol())
                        .securityId(trade.getSecurityId())
                        .tradedQuantity(trade.getTradedQuantity())
                        .tradedPrice(trade.getTradedPrice())
                        .createTime(LocalDateTime.parse(trade.getCreateTime(), DATE_TIME_FORMATTER))
                        .exchangeTime(
                                LocalDateTime.parse(trade.getExchangeTime(), DATE_TIME_FORMATTER))
                        .build();

        dhanTradeRepository.save(dhanTrade);

        return true;
    }
}
