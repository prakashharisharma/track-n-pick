package com.example.service;

import com.example.model.master.Stock;

public interface CandleStickService {


    public boolean isSellingWickPresent(Stock stock);

    public boolean isSellingWickPresent(Stock stock, double benchmark);

    public boolean isSellingWickPresent(double open, double high, double low, double close, double benchmark);

    public boolean isBuyingWickPresent(Stock stock);

    public boolean isBuyingWickPresent(Stock stock, double benchmark);

    public boolean isBuyingWickPresent(double open, double high, double low, double close, double benchmark);

    public boolean isGreen(Stock stock);

    public boolean isRed(Stock stock);

    /**
     * The following conditions enhance the probability
     * Stock must be in overbaught / oversold condition
     * Volume above avg
     * Gap up or gap down next day
     * @param stock
     * @return
     */
    public boolean isDoji(Stock stock);

    /**
     * The following conditions enhance the probability
     * Stock must be in overbaught / oversold condition
     * Volume above avg
     * Gap up or gap down next day
     * @param stock
     * @return
     */
    public boolean isGravestoneDoji(Stock stock);

    public boolean isDragonflyDoji(Stock stock);

    public boolean isHangingMan(Stock stock);

    public boolean isHammer(Stock stock);

    public boolean isShootingStar(Stock stock);

    public boolean isInvertedHammer(Stock stock);

    public boolean isBearishEngulfing(Stock stock);

    public boolean isBullishEngulfing(Stock stock);

    public boolean isBearishMarubozu(Stock stock);

    public boolean isBullishhMarubozu(Stock stock);

    public boolean isBullishOpenEqualPrevClose(Stock stock);

    public boolean isBearishhOpenEqualPrevClose(Stock stock);

    public boolean isTweezerTop(Stock stock);

}
