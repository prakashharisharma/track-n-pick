package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.service.BreakoutService;
import com.example.service.CrossOverUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BreakoutServiceImpl implements BreakoutService {

    @Override
    public boolean isBreakOut20(Stock stock) {

        return CrossOverUtil.isFastCrossesAboveSlow(stock.getStockPrice().getPrevClose(),
                stock.getTechnicals().getPrevEma20(), stock.getStockPrice().getCurrentPrice(),
                stock.getTechnicals().getEma20());
    }

    @Override
    public boolean isBreakOut50(Stock stock) {
        return CrossOverUtil.isFastCrossesAboveSlow(stock.getStockPrice().getPrevClose(),
                stock.getTechnicals().getPrevEma50(), stock.getStockPrice().getCurrentPrice(),
                stock.getTechnicals().getEma50());
    }

    @Override
    public boolean isBreakOut100(Stock stock) {
        return CrossOverUtil.isFastCrossesAboveSlow(stock.getStockPrice().getPrevClose(),
                stock.getTechnicals().getPrevSma100(), stock.getStockPrice().getCurrentPrice(),
                stock.getTechnicals().getSma100());
    }

    @Override
    public boolean isBreakOut200(Stock stock) {
        return CrossOverUtil.isFastCrossesAboveSlow(stock.getStockPrice().getPrevClose(),
                stock.getTechnicals().getPrevSma200(), stock.getStockPrice().getCurrentPrice(),
                stock.getTechnicals().getSma200());
    }

    @Override
    public boolean isBreakDown20(Stock stock) {
        return CrossOverUtil.isSlowCrossesBelowFast(stock.getStockPrice().getPrevClose(),
                stock.getTechnicals().getPrevEma20(), stock.getStockPrice().getCurrentPrice(),
                stock.getTechnicals().getEma20());
    }

    @Override
    public boolean isBreakDown50(Stock stock) {
        return CrossOverUtil.isSlowCrossesBelowFast(stock.getStockPrice().getPrevClose(),
                stock.getTechnicals().getPrevEma50(), stock.getStockPrice().getCurrentPrice(),
                stock.getTechnicals().getEma50());
    }

    @Override
    public boolean isBreakDown100(Stock stock) {
        return CrossOverUtil.isSlowCrossesBelowFast(stock.getStockPrice().getPrevClose(),
                stock.getTechnicals().getPrevSma100(), stock.getStockPrice().getCurrentPrice(),
                stock.getTechnicals().getSma100());
    }

    @Override
    public boolean isBreakDown200(Stock stock) {
        return CrossOverUtil.isSlowCrossesBelowFast(stock.getStockPrice().getPrevClose(),
                stock.getTechnicals().getPrevSma200(), stock.getStockPrice().getCurrentPrice(),
                stock.getTechnicals().getSma200());
    }
}
