package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Portfolio;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.Trade;
import com.example.data.transactional.repo.PortfolioRepository;
import com.example.data.transactional.repo.TradeRepository;
import com.example.data.transactional.view.PortfolioResult;
import com.example.service.*;
import com.example.util.MiscUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PortfolioServiceImpl implements PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final TradeRepository tradeRepository;
    private final UserBrokerageService userBrokerageService;
    private final TaxMasterService taxMasterService;

    private final StockService stockService;

    private final StockPriceService<StockPrice> stockPriceService;

    private final MiscUtil miscUtil;

    @Override
    public BigDecimal getTotalInvestmentValue(Long userId) {
        // Get all portfolios for the user
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);

        // Calculate the total investment value
        BigDecimal totalInvestmentValue =
                portfolios.stream()
                        .map(
                                portfolio ->
                                        portfolio
                                                .getAvgPrice()
                                                .multiply(
                                                        BigDecimal.valueOf(
                                                                portfolio.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalInvestmentValue;
    }

    @Transactional
    public void buyStock(Long userId, Long stockId, long quantity, BigDecimal price) {

        Stock stock = stockService.getStockById(stockId);

        BigDecimal stt =
                calculateTax(price, quantity, taxMasterService.getTaxMaster().getSecurityTxnTax());
        BigDecimal stampDuty =
                calculateTax(price, quantity, taxMasterService.getTaxMaster().getStampDuty());
        BigDecimal exchangeTxnCharge =
                calculateTax(
                        price, quantity, taxMasterService.getTaxMaster().getNseTransactionCharge());
        BigDecimal sebiTurnoverFee =
                calculateTax(price, quantity, taxMasterService.getTaxMaster().getSebiTurnoverFee());
        BigDecimal dpCharge = BigDecimal.ZERO;
        BigDecimal brokerage =
                userBrokerageService.calculate(userId, price.doubleValue(), quantity);
        BigDecimal gst =
                calculateGst(
                        brokerage,
                        exchangeTxnCharge,
                        sebiTurnoverFee,
                        dpCharge,
                        taxMasterService.getTaxMaster().getGst());

        BigDecimal effectivePrice =
                price.add(stt)
                        .add(stampDuty)
                        .add(exchangeTxnCharge)
                        .add(sebiTurnoverFee)
                        .add(dpCharge)
                        .add(brokerage)
                        .add(gst);

        Trade trade =
                Trade.builder()
                        .userId(userId)
                        .stock(stock)
                        .type(Trade.Type.BUY)
                        .quantity(quantity)
                        .price(price)
                        .effectivePrice(effectivePrice)
                        .realizedPnl(BigDecimal.ZERO)
                        .stt(stt)
                        .stampDuty(stampDuty)
                        .exchangeTxnCharge(exchangeTxnCharge)
                        .sebiTurnoverFee(sebiTurnoverFee)
                        .dpCharge(dpCharge)
                        .brokerage(brokerage)
                        .gst(gst)
                        .sessionDate(LocalDate.now())
                        .timestamp(LocalDateTime.now())
                        .build();
        tradeRepository.save(trade);
        Portfolio portfolio =
                portfolioRepository.findByUserIdAndStock(userId, stock).orElse(new Portfolio());
        updatePortfolio(portfolio, userId, stock, quantity, effectivePrice);
        portfolioRepository.save(portfolio);
    }

    @Transactional
    public void sellStock(Long userId, Long stockId, long quantity, BigDecimal price) {

        Stock stock = stockService.getStockById(stockId);

        Portfolio portfolio =
                portfolioRepository
                        .findByUserIdAndStock(userId, stock)
                        .orElseThrow(() -> new RuntimeException("Insufficient stocks to sell"));

        if (portfolio.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stocks in portfolio");
        }

        List<Trade> buyTrades =
                tradeRepository.findByUserIdAndStockAndTypeOrderByTimestampAsc(
                        userId, stock, Trade.Type.BUY);

        BigDecimal realizedPnL = BigDecimal.ZERO;
        long remainingQuantity = quantity;

        BigDecimal stt =
                calculateTax(price, quantity, taxMasterService.getTaxMaster().getSecurityTxnTax());
        BigDecimal stampDuty =
                calculateTax(price, quantity, taxMasterService.getTaxMaster().getStampDuty());
        BigDecimal exchangeTxnCharge =
                calculateTax(
                        price, quantity, taxMasterService.getTaxMaster().getNseTransactionCharge());
        BigDecimal sebiTurnoverFee =
                calculateTax(price, quantity, taxMasterService.getTaxMaster().getSebiTurnoverFee());
        BigDecimal dpCharge = userBrokerageService.getDpCharge(userId);
        BigDecimal brokerage =
                userBrokerageService.calculate(userId, price.doubleValue(), quantity);
        BigDecimal gst =
                calculateGst(
                        brokerage,
                        exchangeTxnCharge,
                        sebiTurnoverFee,
                        dpCharge,
                        taxMasterService.getTaxMaster().getGst());

        BigDecimal totalCost =
                stt.add(stampDuty)
                        .add(exchangeTxnCharge)
                        .add(sebiTurnoverFee)
                        .add(dpCharge)
                        .add(brokerage)
                        .add(gst);

        BigDecimal perUnitCost =
                totalCost.divide(BigDecimal.valueOf(quantity), 6, RoundingMode.HALF_UP);
        BigDecimal sellEffectivePrice = price.subtract(perUnitCost);

        for (Trade buyTrade : buyTrades) {
            if (remainingQuantity <= 0) break;

            long sellQuantity = Math.min(buyTrade.getQuantity(), remainingQuantity);
            remainingQuantity -= sellQuantity;

            BigDecimal buyCost = buyTrade.getEffectivePrice();
            BigDecimal profit =
                    sellEffectivePrice.subtract(buyCost).multiply(BigDecimal.valueOf(sellQuantity));
            realizedPnL = realizedPnL.add(profit);

            buyTrade.setQuantity(buyTrade.getQuantity() - sellQuantity);
            if (buyTrade.getQuantity() == 0) tradeRepository.delete(buyTrade);
            else tradeRepository.save(buyTrade);
        }

        if (remainingQuantity > 0)
            throw new RuntimeException("Not enough FIFO buy trades to match sell");

        Trade trade =
                Trade.builder()
                        .userId(userId)
                        .stock(stock)
                        .type(Trade.Type.SELL)
                        .quantity(quantity)
                        .price(price)
                        .effectivePrice(sellEffectivePrice)
                        .realizedPnl(realizedPnL)
                        .stt(stt)
                        .stampDuty(stampDuty)
                        .exchangeTxnCharge(exchangeTxnCharge)
                        .sebiTurnoverFee(sebiTurnoverFee)
                        .dpCharge(dpCharge)
                        .brokerage(brokerage)
                        .gst(gst)
                        .sessionDate(LocalDate.now())
                        .timestamp(LocalDateTime.now())
                        .build();

        tradeRepository.save(trade);

        portfolio.setQuantity(portfolio.getQuantity() - quantity);
        if (portfolio.getQuantity() == 0) {
            portfolioRepository.delete(portfolio);
        } else {
            portfolioRepository.save(portfolio);
        }
    }

    private void updatePortfolio(
            Portfolio portfolio,
            Long userId,
            Stock stock,
            long quantity,
            BigDecimal effectivePrice) {
        if (portfolio.getUserId() == null) {
            portfolio.setUserId(userId);
            portfolio.setStock(stock);
            portfolio.setQuantity(quantity);
            portfolio.setAvgPrice(effectivePrice);
        } else {
            long existingQuantity = portfolio.getQuantity();
            BigDecimal existingInvestment =
                    portfolio.getAvgPrice().multiply(BigDecimal.valueOf(existingQuantity));
            BigDecimal newInvestment = effectivePrice.multiply(BigDecimal.valueOf(quantity));

            long updatedQuantity = existingQuantity + quantity;
            BigDecimal updatedAvgPrice =
                    existingInvestment
                            .add(newInvestment)
                            .divide(BigDecimal.valueOf(updatedQuantity), RoundingMode.HALF_UP);

            portfolio.setQuantity(updatedQuantity);
            portfolio.setAvgPrice(updatedAvgPrice);
        }
    }

    private BigDecimal calculateTax(BigDecimal price, long quantity, double taxRate) {
        return price.multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.valueOf(taxRate))
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateGst(
            BigDecimal brokerage,
            BigDecimal exchangeTxnCharge,
            BigDecimal sebiTurnoverFee,
            BigDecimal dpCharge,
            double gstRate) {
        return brokerage
                .add(exchangeTxnCharge)
                .add(sebiTurnoverFee)
                .add(dpCharge)
                .multiply(BigDecimal.valueOf(gstRate))
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }

    @Override
    public Page<PortfolioResult> get(
            Long userId, int page, int size, String sortBy, String direction) {
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);

        List<PortfolioResult> resultList =
                portfolios.stream()
                        .map(
                                portfolio -> {
                                    Stock stock = portfolio.getStock();
                                    StockPrice stockPrice =
                                            stockPriceService.get(stock, Timeframe.DAILY);
                                    double currentPrice = stockPrice.getClose();
                                    double prevClose = stockPrice.getPrevClose();
                                    double changePercent =
                                            prevClose != 0
                                                    ? ((currentPrice - prevClose) / prevClose) * 100
                                                    : 0;

                                    return mapToPortfolioResult(
                                            portfolio, currentPrice, changePercent);
                                })
                        .sorted(getComparator(sortBy, direction))
                        .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, resultList.size());

        List<PortfolioResult> pageContent =
                start < end ? resultList.subList(start, end) : Collections.emptyList();

        return new PageImpl<>(pageContent, PageRequest.of(page, size), resultList.size());
    }

    public PortfolioResult mapToPortfolioResult(
            Portfolio portfolio, double currentPrice, double changePercent) {
        BigDecimal avgPrice = portfolio.getAvgPrice();
        long quantity = portfolio.getQuantity();
        double investment = avgPrice.doubleValue() * quantity;
        double currentValue = currentPrice * quantity;
        double pnlPercent = investment == 0 ? 0 : ((currentValue - investment) / investment) * 100;

        Stock stock = portfolio.getStock();

        return PortfolioResult.builder()
                .id(stock.getStockId())
                .symbol(stock.getNseSymbol())
                .name(stock.getCompanyName())
                .price(currentPrice)
                .changePercent(miscUtil.roundToTwoDecimals(changePercent))
                .sector(stock.getSectorName())
                .quantity(quantity)
                .averagePrice(miscUtil.roundToTwoDecimals(avgPrice.doubleValue()))
                .investment(miscUtil.roundToTwoDecimals(investment))
                .currentValue(miscUtil.roundToTwoDecimals(currentValue))
                .pnlPercent(miscUtil.roundToTwoDecimals(pnlPercent))
                .build();
    }

    private Comparator<PortfolioResult> getComparator(String sortBy, String direction) {
        Comparator<PortfolioResult> comparator;

        switch (sortBy) {
            case "name" -> comparator =
                    Comparator.comparing(PortfolioResult::getName, String.CASE_INSENSITIVE_ORDER);
            case "changePercent" -> comparator =
                    Comparator.comparingDouble(PortfolioResult::getChangePercent);
            case "quantity" -> comparator = Comparator.comparingLong(PortfolioResult::getQuantity);
            case "investment" -> comparator =
                    Comparator.comparingDouble(PortfolioResult::getInvestment);
            case "currentValue" -> comparator =
                    Comparator.comparingDouble(PortfolioResult::getCurrentValue);
            case "pnlPercent" -> comparator =
                    Comparator.comparingDouble(PortfolioResult::getPnlPercent);
            default -> comparator =
                    Comparator.comparing(PortfolioResult::getSymbol, String.CASE_INSENSITIVE_ORDER);
        }

        return "desc".equalsIgnoreCase(direction) ? comparator.reversed() : comparator;
    }

    @Override
    public PortfolioResult stats(Long userId) {

        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);

        double totalInvestment = 0.0;
        double totalCurrentValue = 0.0;

        for (Portfolio portfolio : portfolios) {
            Stock stock = portfolio.getStock();
            StockPrice price = stockPriceService.get(stock, Timeframe.DAILY);

            double avgPrice = portfolio.getAvgPrice().doubleValue();
            long quantity = portfolio.getQuantity();
            double investment = avgPrice * quantity;
            double currentValue = price.getClose() * quantity;

            totalInvestment += investment;
            totalCurrentValue += currentValue;
        }

        double pnlPercent =
                totalInvestment == 0
                        ? 0
                        : ((totalCurrentValue - totalInvestment) / totalInvestment) * 100;
        double pnl = totalInvestment - totalCurrentValue;

        return PortfolioResult.builder()
                .investment(miscUtil.roundToTwoDecimals(totalInvestment))
                .currentValue(miscUtil.roundToTwoDecimals(totalCurrentValue))
                .pnlPercent(miscUtil.roundToTwoDecimals(pnlPercent))
                .pnl(miscUtil.roundToTwoDecimals(pnl))
                .build();
    }
}
