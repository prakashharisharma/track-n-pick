package com.example.service;

import com.example.model.master.Stock;
import com.example.util.FibonacciRatio;

public interface CandleStickService {


    public static final double DEFAULT_SELLING_WICK_PER = 21.0;

    public static final double BUYING_WICK_PER = 70.0;

    public static final double MIN_WICK_PER = 68.0;

    public static final double MAX_WICK_SIZE = FibonacciRatio.RATIO_78_6 * 10;

    //FibonacciRatio.RATIO_161_8 * 10;  //2.0;
    public static final double MIN_BODY_SIZE = (FibonacciRatio.RATIO_23_6 * 100)/2;
    //FibonacciRatio.RATIO_261_8 * 10;// 3.0;
    public static final double MIN_RANGE = (FibonacciRatio.RATIO_38_2 * 100)/2;

    public static final double MAX_RANGE = 20.0;

    //public static final double EPSILON_PER = FormulaService.EPSILON_PER;

    public static final double EPSILON_PER = 0.1618;

    public boolean isDead(Stock stock);

    public double upperWickSize(Stock stock);

    public double lowerWickSize(Stock stock);

    public double body(Stock stock);

    public double bodyPrev(Stock stock);

    public double range(Stock stock);

    public double rangePrev(Stock stock);

    public boolean isCloseAbovePrevClose(Stock stock);

    public boolean isCloseBelowPrevClose(Stock stock);

    public boolean isOpenAbovePrevClose(Stock stock);

    public boolean isOpenBelowPrevClose(Stock stock);

    public boolean isOpenAtPrevOpen(Stock stock);

    public boolean isOpenAtPrevClose(Stock stock);

    public boolean isOpenAbovePrevOpen(Stock stock);

    public boolean isOpenBelowPrevOpen(Stock stock);

    public boolean isCloseAbovePrevOpen(Stock stock);
    public boolean isCloseBelowPrevOpen(Stock stock);

    public boolean isOpenAndLowEqual(Stock stock);

    public boolean isOpenAndHighEqual(Stock stock);

    public boolean isCloseAndLowEqual(Stock stock);

    public boolean isCloseAndHighEqual(Stock stock);

    public boolean isCloseBelowPrevLow(Stock stock);

    public boolean isHigherHigh(Stock stock);

    public boolean isHigherLow(Stock stock);

    public boolean isLowerHigh(Stock stock);

    public boolean isLowerLow(Stock stock);

    public boolean isSellingWickPresent(Stock stock);

    public boolean isSellingWickPresent(Stock stock, double benchmark);

    public boolean isSellingWickPresent(double open, double high, double low, double close, double benchmark);

    public boolean isBuyingWickPresent(Stock stock);

    public boolean isBuyingWickPresent(Stock stock, double benchmark);

    public boolean isBuyingWickPresent(double open, double high, double low, double close, double benchmark);

    public boolean isGreen(Stock stock);

    public boolean isGapUp(Stock stock);

    public boolean isGapDown(Stock stock);

    public boolean isRisingWindow(Stock stock);
    public boolean isFallingWindow(Stock stock);

    public boolean isPreviousDayGreen(Stock stock);
    public boolean isRed(Stock stock);
    public boolean isPreviousDayRed(Stock stock);

    /**
     * The following conditions enhance the probability
     * Stock must be in overbaught / oversold condition
     * Volume above avg
     * Gap up or gap down next day
     * @param stock
     * @return
     */
    public boolean isDoji(Stock stock);
    public boolean isDojiPrev(Stock stock);
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


    public boolean isBullishPinBar(Stock stock);

    public boolean isBearishPinBar(Stock stock);

    public boolean isSpinningTop(Stock stock);

    public boolean isSpinningTopPrev(Stock stock);

    public boolean isInDecision(Stock stock);

    public boolean isInDecisionPrev(Stock stock);

    public boolean isInDecisionPrevConfirmationBullish(Stock stock);

    public boolean isInDecisionPrevConfirmationBearish(Stock stock);

    /**
     * Need confirmation
     * @param stock
     * @return
     */
    public boolean isHangingMan(Stock stock);

    public boolean isHammer(Stock stock);

    public boolean isShootingStar(Stock stock);

    /**
     * Need confirmation
     * @param stock
     * @return
     */
    public boolean isInvertedHammer(Stock stock);

    public boolean isOpenHigh(Stock stock);

    public boolean isOpenLow(Stock stock);

    public boolean isBearishEngulfing(Stock stock);

    public boolean isBullishEngulfing(Stock stock);

    public boolean isBullishOutsideBar(Stock stock);

    public boolean isBearishOutsideBar(Stock stock);

    public boolean isBearishMarubozu(Stock stock);

    public boolean isBullishhMarubozu(Stock stock);

    public boolean isTweezerTop(Stock stock);

    public boolean isDoubleHigh(Stock stock);

    public boolean isDoubleTop(Stock stock);

    public boolean isTweezerBottom(Stock stock);
    public boolean isDoubleLow(Stock stock);

    public boolean isDoubleBottom(Stock stock);
    public boolean isDarkCloudCover(Stock stock);

    public boolean isPiercingPattern(Stock stock);

    public boolean isBullishKicker(Stock stock);

    public boolean isBearishKicker(Stock stock);

    public boolean isBullishSash(Stock stock);

    public boolean isBearishSash(Stock stock);

    public boolean isBullishSeparatingLine(Stock stock);

    public boolean isBearishSeparatingLine(Stock stock);

    /**
     * Need confirmation
     * @param stock
     * @return
     */
    public boolean isBearishHarami(Stock stock);

    /**
     * Need confirmation
     * @param stock
     * @return
     */
    public boolean isBullishHarami(Stock stock);

    /**
     * Need confirmation
     * @param stock
     * @return
     */
    public boolean isBullishInsideBar(Stock stock);

    /**
     * Need confirmation
     * @param stock
     * @return
     */
    public boolean isBearishInsideBar(Stock stock);

    public boolean isBullishSoldiers(Stock stock);

    public boolean isBearishSoldiers(Stock stock);
}
