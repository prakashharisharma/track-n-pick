package com.example.service.dhan;

import com.example.data.transactional.entities.DhanTrade;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.entities.type.dhan.TransactionType;
import com.example.data.transactional.repo.DhanTradeRepository;
import com.example.external.dhan.model.Trade;
import com.example.service.CalendarService;
import com.example.service.ResearchTechnicalService;
import com.example.service.StockService;
import java.time.LocalDate;
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
    private final ResearchTechnicalService researchTechnicalService;
    private final CalendarService calendarService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public boolean processTrade(Trade trade, User user) {

        // Check if trade already exists
        Optional<DhanTrade> existingTrade =
                dhanTradeRepository.findByExchangeTradeIdAndOrderId(
                        trade.getExchangeTradeId(), trade.getOrderId());

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
                        .charges(this.calculateCharges(trade))
                        .createTime(LocalDateTime.parse(trade.getCreateTime(), DATE_TIME_FORMATTER))
                        .exchangeTime(
                                LocalDateTime.parse(trade.getExchangeTime(), DATE_TIME_FORMATTER))
                        .build();

        dhanTradeRepository.save(dhanTrade);

        return true;
    }

    private double calculateCharges(Trade trade) {

        if (trade.getTransactionType() == TransactionType.SELL) {
            Stock stock = stockService.getStockByNseSymbol(trade.getTradingSymbol());
            Optional<ResearchTechnical> researchTechnicalOptional =
                    researchTechnicalService.getLatest(stock);
            if (researchTechnicalOptional.isPresent()) {
                ResearchTechnical researchTechnical = researchTechnicalOptional.get();

                if (researchTechnical.getResearchDate()
                        == calendarService.previousTradingSession(LocalDate.now())) {
                    double tradedValue = trade.getTradedQuantity() * trade.getTradedPrice();
                    double profitMargin = tradedValue * 0.001; // .001% of traded value for intraday
                    return profitMargin + 2.0; // 2% of traded value + INR 2 per trade
                }

                // SELL - In profit charge 2% of profit + INR 2 per trade
                else if (researchTechnical.getEntryPrice() < trade.getTradedPrice()) {
                    double tradedValue = trade.getTradedQuantity() * trade.getTradedPrice();
                    double researchEntryValue =
                            trade.getTradedQuantity() * researchTechnical.getEntryPrice();
                    double profitMargin = (tradedValue - researchEntryValue) * 0.02;
                    return profitMargin + 2.0; // 2% of traded value + INR 2 per trade
                }
                // SELL - In loss only charge INR .50 per trade
                return 0.5;
            }
            // SELL - Outside research charge INR 5 per trade
            return 5.0;
        }
        // BUY - Charge INR 2 per trade
        return 2.0;
    }
}
