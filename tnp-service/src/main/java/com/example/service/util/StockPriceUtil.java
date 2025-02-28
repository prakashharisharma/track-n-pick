package com.example.service.util;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;

import java.time.LocalDate;

public class StockPriceUtil {

    public static Stock buildStockPrice(String nseSymbol, double open, double high, double low, double close){

        StockPrice stockPrice = new StockPrice();

        stockPrice.setOpen(open);
        stockPrice.setHigh(high);
        stockPrice.setLow(low);
        stockPrice.setClose(close);

        Stock stock = new Stock();
        stock.setNseSymbol(nseSymbol+":T-1");
        stock.setStockPrice(stockPrice);

        return stock;
    }

    public static Stock buildStockPrice(String nseSymbol,  double open, double high, double low, double close,
                                        double prevOpen, double prevHigh, double prevLow, double prevClose){

        StockPrice stockPrice = new StockPrice();

        stockPrice.setOpen(open);
        stockPrice.setHigh(high);
        stockPrice.setLow(low);
        stockPrice.setClose(close);

        stockPrice.setPrevOpen(prevOpen);
        stockPrice.setPrevHigh(prevHigh);
        stockPrice.setPrevLow(prevLow);
        stockPrice.setPrevClose(prevClose);

        Stock stock = new Stock();
        stock.setNseSymbol(nseSymbol+":T-1");
        stock.setStockPrice(stockPrice);

        return stock;
    }

    public static Stock buildStockPrice(String nseSymbol, LocalDate bhavDate, double open, double high, double low, double close,
                                        double prevOpen, double prevHigh, double prevLow, double prevClose){

        StockPrice stockPrice = new StockPrice();

        stockPrice.setBhavDate(bhavDate);
        stockPrice.setOpen(open);
        stockPrice.setHigh(high);
        stockPrice.setLow(low);
        stockPrice.setClose(close);

        stockPrice.setPrevOpen(prevOpen);
        stockPrice.setPrevHigh(prevHigh);
        stockPrice.setPrevLow(prevLow);
        stockPrice.setPrevClose(prevClose);

        Stock stock = new Stock();
        stock.setNseSymbol(nseSymbol+":"+bhavDate);
        stock.setStockPrice(stockPrice);

        return stock;
    }

    public static Stock buildStockPricePreviousSession(Stock stock){

        StockPrice stockPricePreviousSession = new StockPrice();

        stockPricePreviousSession.setBhavDate(stock.getStockPrice().getBhavDatePrev());
        stockPricePreviousSession.setOpen(stock.getStockPrice().getPrevOpen());
        stockPricePreviousSession.setHigh(stock.getStockPrice().getPrevHigh());
        stockPricePreviousSession.setLow(stock.getStockPrice().getPrevLow());
        stockPricePreviousSession.setClose(stock.getStockPrice().getPrevClose());

        stockPricePreviousSession.setPrevOpen(stock.getStockPrice().getPrevPrevOpen());
        stockPricePreviousSession.setPrevHigh(stock.getStockPrice().getPrevPrevClose());
        stockPricePreviousSession.setPrevLow(stock.getStockPrice().getPrevPrevLow());
        stockPricePreviousSession.setPrevClose(stock.getStockPrice().getPrevPrevClose());

        Stock stockPreviousSession = new Stock();
        stockPreviousSession.setNseSymbol(stock.getNseSymbol());
        stockPreviousSession.setStockPrice(stockPricePreviousSession);

        return stockPreviousSession;
    }

    public static Stock buildStockPricePreviousWeek(Stock stock, LocalDate previousWeekFirstDay){

        StockTechnicals stockTechnicals = stock.getTechnicals();
        StockPrice stockPricePreviousWeek = new StockPrice();

        stockPricePreviousWeek.setBhavDate(previousWeekFirstDay);
        stockPricePreviousWeek.setOpen(stockTechnicals.getPrevWeekOpen());
        stockPricePreviousWeek.setHigh(stockTechnicals.getPrevWeekHigh());
        stockPricePreviousWeek.setLow(stockTechnicals.getPrevWeekLow());
        stockPricePreviousWeek.setClose(stockTechnicals.getPrevWeekClose());

        stockPricePreviousWeek.setPrevOpen(stockTechnicals.getPrevPrevWeekOpen());
        stockPricePreviousWeek.setPrevHigh(stockTechnicals.getPrevPrevWeekHigh());
        stockPricePreviousWeek.setPrevLow(stockTechnicals.getPrevPrevWeekLow());
        stockPricePreviousWeek.setPrevClose(stockTechnicals.getPrevPrevWeekClose());

        Stock stockPreviousWeek = new Stock();
        stockPreviousWeek.setNseSymbol(stock.getNseSymbol());
        stockPreviousWeek.setStockPrice(stockPricePreviousWeek);

        return stockPreviousWeek;
    }

    public static Stock buildStockPricePreviousMonth(Stock stock, LocalDate previousMonthFirstDay){

        StockTechnicals stockTechnicals = stock.getTechnicals();
        StockPrice stockPricePreviousWeek = new StockPrice();

        stockPricePreviousWeek.setBhavDate(previousMonthFirstDay);
        stockPricePreviousWeek.setOpen(stockTechnicals.getPrevMonthOpen());
        stockPricePreviousWeek.setHigh(stockTechnicals.getPrevMonthHigh());
        stockPricePreviousWeek.setLow(stockTechnicals.getPrevMonthLow());
        stockPricePreviousWeek.setClose(stockTechnicals.getPrevMonthClose());

        stockPricePreviousWeek.setPrevOpen(stockTechnicals.getPrevPrevMonthOpen());
        stockPricePreviousWeek.setPrevHigh(stockTechnicals.getPrevPrevMonthHigh());
        stockPricePreviousWeek.setPrevLow(stockTechnicals.getPrevPrevMonthLow());
        stockPricePreviousWeek.setPrevClose(stockTechnicals.getPrevPrevMonthClose());

        Stock stockPreviousWeek = new Stock();
        stockPreviousWeek.setNseSymbol(stock.getNseSymbol());
        stockPreviousWeek.setStockPrice(stockPricePreviousWeek);

        return stockPreviousWeek;
    }

    public static Stock buildStockPricePreviousQuarter(Stock stock, LocalDate previousQuarterFirstDay){

        StockTechnicals stockTechnicals = stock.getTechnicals();
        StockPrice stockPricePreviousWeek = new StockPrice();

        stockPricePreviousWeek.setBhavDate(previousQuarterFirstDay);
        stockPricePreviousWeek.setOpen(stockTechnicals.getPrevQuarterOpen());
        stockPricePreviousWeek.setHigh(stockTechnicals.getPrevQuarterHigh());
        stockPricePreviousWeek.setLow(stockTechnicals.getPrevQuarterLow());
        stockPricePreviousWeek.setClose(stockTechnicals.getPrevQuarterClose());

        stockPricePreviousWeek.setPrevOpen(stockTechnicals.getPrevPrevQuarterOpen());
        stockPricePreviousWeek.setPrevHigh(stockTechnicals.getPrevPrevQuarterHigh());
        stockPricePreviousWeek.setPrevLow(stockTechnicals.getPrevPrevQuarterLow());
        stockPricePreviousWeek.setPrevClose(stockTechnicals.getPrevPrevQuarterClose());

        Stock stockPreviousWeek = new Stock();
        stockPreviousWeek.setNseSymbol(stock.getNseSymbol());
        stockPreviousWeek.setStockPrice(stockPricePreviousWeek);

        return stockPreviousWeek;
    }
}
