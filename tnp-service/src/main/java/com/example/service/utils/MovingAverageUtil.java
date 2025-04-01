package com.example.service.utils;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockTechnicals;

public class MovingAverageUtil {

    public static double getMovingAverage5(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma5();
    }

    public static double getMovingAverage10(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma10();
    }

    public static double getMovingAverage20(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma20();
    }

    public static double getMovingAverage50(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return (timeframe == Timeframe.MONTHLY)
                ? stockTechnicals.getSma50()
                : stockTechnicals.getEma50();
    }

    public static double getMovingAverage100(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getSma100()
                : stockTechnicals.getEma100();
    }

    public static double getMovingAverage200(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getSma200()
                : stockTechnicals.getEma200();
    }

    public static double getPrevMovingAverage5(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma5();
    }

    public static double getPrevMovingAverage10(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma10();
    }

    public static double getPrevMovingAverage20(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma20();
    }

    public static double getPrevMovingAverage50(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return (timeframe == Timeframe.MONTHLY)
                ? stockTechnicals.getPrevSma50()
                : stockTechnicals.getPrevEma50();
    }

    public static double getPrevMovingAverage100(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getPrevSma100()
                : stockTechnicals.getPrevEma100();
    }

    public static double getPrevMovingAverage200(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getPrevSma200()
                : stockTechnicals.getPrevEma200();
    }
}
