package com.example.service.impl;

import com.example.data.transactional.entities.Portfolio;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.Trade;
import com.example.data.transactional.repo.PortfolioRepository;
import com.example.data.transactional.repo.TradeRepository;
import com.example.service.PortfolioService;
import com.example.service.TaxMasterService;
import com.example.service.UserBrokerageService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public void buyStock(Long userId, Stock stock, long quantity, BigDecimal price) {
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

        Portfolio portfolio =
                portfolioRepository.findByUserIdAndStock(userId, stock).orElse(new Portfolio());
        updatePortfolio(portfolio, userId, stock, quantity, effectivePrice);
        portfolioRepository.save(portfolio);
    }

    @Transactional
    public void sellStock(Long userId, Stock stock, long quantity, BigDecimal price) {
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

        BigDecimal sellEffectivePrice =
                price.subtract(stt)
                        .subtract(stampDuty)
                        .subtract(exchangeTxnCharge)
                        .subtract(sebiTurnoverFee)
                        .subtract(dpCharge)
                        .subtract(brokerage)
                        .subtract(gst);

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
        if (portfolio.getQuantity() == 0) portfolioRepository.delete(portfolio);
        else portfolioRepository.save(portfolio);
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
}
