package com.example.service.impl;

import com.example.data.transactional.entities.Trade;
import com.example.data.transactional.repo.TradeRepository;
import com.example.data.transactional.view.TradeResult;
import com.example.service.TradeService;
import com.example.service.TradeSortHelper;
import com.example.util.MiscUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;

    private final MiscUtil miscUtil;

    @Override
    public Page<TradeResult> getUserTrades(
            Long userId,
            int page,
            int size,
            String q,
            Trade.Type type,
            LocalDate from,
            LocalDate to,
            String sortBy,
            String direction) {
        Sort sort = TradeSortHelper.resolveSort(sortBy, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Trade> trades = tradeRepository.searchTrades(userId, q, type, from, to, pageable);

        return trades.map(this::mapToTradeResult);
    }

    private TradeResult mapToTradeResult(Trade trade) {
        BigDecimal taxes =
                trade.getStt()
                        .add(trade.getStampDuty())
                        .add(trade.getExchangeTxnCharge())
                        .add(trade.getSebiTurnoverFee())
                        .add(trade.getDpCharge())
                        .add(trade.getGst());

        BigDecimal net = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
        BigDecimal gross = BigDecimal.ZERO;
        if (trade.getType() == Trade.Type.BUY) {
            gross = net.add(taxes);
        } else {
            gross = net.subtract(taxes);
        }

        return TradeResult.builder()
                .symbol(trade.getStock().getNseSymbol())
                .name(trade.getStock().getCompanyName())
                .type(trade.getType())
                .quantity(trade.getQuantity())
                .price(trade.getPrice().doubleValue())
                .net(miscUtil.roundToTwoDecimals(net.doubleValue()))
                .taxes(miscUtil.roundToTwoDecimals(taxes.doubleValue()))
                .brokerage(miscUtil.roundToTwoDecimals(trade.getBrokerage().doubleValue()))
                .gross(miscUtil.roundToTwoDecimals(gross.doubleValue()))
                .realizedPnl(miscUtil.roundToTwoDecimals(trade.getRealizedPnl().doubleValue()))
                .date(trade.getSessionDate())
                .build();
    }
}
