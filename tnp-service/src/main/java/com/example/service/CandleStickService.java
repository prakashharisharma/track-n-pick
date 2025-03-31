package com.example.service;


import com.example.data.transactional.entities.StockPrice;
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




    public boolean isDead(StockPrice stockPrice);

    public double upperWickSize(StockPrice stockPrice);

    public double lowerWickSize(StockPrice stockPrice);

    public double currentBodySize(StockPrice stockPrice);

    public double prevBodySize(StockPrice stockPrice);

    public double range(StockPrice stockPrice);

    public double prevRange(StockPrice stockPrice);
    public boolean hasLongLowerWick(StockPrice stockPrice);

    public boolean isOpenInsidePrevBody(StockPrice stockPrice);

    public boolean isCloseInsidePrevBody(StockPrice stockPrice);

    public boolean isCloseAbovePrevClose(StockPrice stockPrice);

    public boolean isCloseBelowPrevClose(StockPrice stockPrice);

    public boolean isOpenAbovePrevClose(StockPrice stockPrice);

    public boolean isOpenBelowPrevClose(StockPrice stockPrice);

    public boolean isOpenAtPrevOpen(StockPrice stockPrice);

    public boolean isOpenAtPrevClose(StockPrice stockPrice);

    public boolean isOpenAbovePrevOpen(StockPrice stockPrice);

    public boolean isOpenBelowPrevOpen(StockPrice stockPrice);

    public boolean isCloseAbovePrevOpen(StockPrice stockPrice);
    public boolean isCloseBelowPrevOpen(StockPrice stockPrice);

    public boolean isOpenAndLowEqual(StockPrice stockPrice);

    public boolean isOpenAndHighEqual(StockPrice stockPrice);

    public boolean isCloseAndLowEqual(StockPrice stockPrice);

    public boolean isCloseAndHighEqual(StockPrice stockPrice);

    public boolean isCloseBelowPrevLow(StockPrice stockPrice);

    public boolean isHigherHigh(StockPrice stockPrice);

    public boolean isHigherLow(StockPrice stockPrice);

    public boolean isPrevHigherHigh(StockPrice stockPrice);

    public boolean isPrevHigherLow(StockPrice stockPrice);

    public boolean isLowerHigh(StockPrice stockPrice);


    public boolean isLowerLow(StockPrice stockPrice);

    public boolean isPrevLowerHigh(StockPrice stockPrice);
    public boolean isPrevLowerLow(StockPrice stockPrice);

    public boolean isSellingWickPresent(StockPrice stockPrice);

    public boolean isSellingWickPresent(StockPrice stockPrice, double benchmark);

    public boolean isBuyingWickPresent(StockPrice stockPrice);

    public boolean isBuyingWickPresent(StockPrice stockPrice, double benchmark);


    public boolean isGapUp(StockPrice stockPrice);

    public boolean isGapDown(StockPrice stockPrice);

    public boolean isRisingWindow(StockPrice stockPrice);
    public boolean isFallingWindow(StockPrice stockPrice);

    public boolean isGreen(StockPrice stockPrice);

    public boolean isPreviousSessionGreen(StockPrice stockPrice);
    public boolean isRed(StockPrice stockPrice);
    public boolean isPreviousSessionRed(StockPrice stockPrice);

    /**
     * The following conditions enhance the probability
     * Stock must be in overbaught / oversold condition
     * Volume above avg
     * Gap up or gap down next day
     * @param stockPrice
     * @return
     */
    public boolean isDoji(StockPrice stockPrice);
    public boolean isPrevDoji(StockPrice stockPrice);
    /**
     * The following conditions enhance the probability
     * Stock must be in overbaught / oversold condition
     * Volume above avg
     * Gap up or gap down next day
     * @param stockPrice
     * @return
     */
    public boolean isGravestoneDoji(StockPrice stockPrice);

    public boolean isDragonflyDoji(StockPrice stockPrice);


    public boolean isBullishPinBar(StockPrice stockPrice);

    public boolean isBearishPinBar(StockPrice stockPrice);

    public boolean isSpinningTop(StockPrice stockPrice);

    public boolean isPrevSpinningTop(StockPrice stockPrice);

    public boolean isInDecision(StockPrice stockPrice);

    public boolean isPrevInDecision(StockPrice stockPrice);

    public boolean isPrevInDecisionConfirmationBullish(StockPrice stockPrice);

    public boolean isPrevInDecisionConfirmationBearish(StockPrice stockPrice);

    /**
     * Need confirmation
     * @param stockPrice
     * @return
     */
    public boolean isHangingMan(StockPrice stockPrice);

    public boolean isHammer(StockPrice stockPrice);

    public boolean isShootingStar(StockPrice stockPrice);

    /**
     * Need confirmation
     * @param stockPrice
     * @return
     */
    public boolean isInvertedHammer(StockPrice stockPrice);

    public boolean isOpenHigh(StockPrice stockPrice);

    public boolean isOpenLow(StockPrice stockPrice);
    public boolean isBearishMarubozu(StockPrice stockPrice);

    public boolean isBullishMarubozu(StockPrice stockPrice);


    public boolean isBearishEngulfing(StockPrice stockPrice);

    public boolean isBullishEngulfing(StockPrice stockPrice);

    public boolean isBullishOutsideBar(StockPrice stockPrice);

    public boolean isBearishOutsideBar(StockPrice stockPrice);

    public boolean isTweezerTop(StockPrice stockPrice);

    public boolean isDoubleTop(StockPrice stockPrice);


    public boolean isTweezerBottom(StockPrice stockPrice);
    public boolean isDoubleBottom(StockPrice stockPrice);

    public boolean isDarkCloudCover(StockPrice stockPrice);

    public boolean isPiercingPattern(StockPrice stockPrice);

    public boolean isBullishKicker(StockPrice stockPrice);

    public boolean isBearishKicker(StockPrice stockPrice);

    public boolean isBullishSash(StockPrice stockPrice);

    public boolean isBearishSash(StockPrice stockPrice);

    public boolean isBullishSeparatingLine(StockPrice stockPrice);

    public boolean isBearishSeparatingLine(StockPrice stockPrice);

    /**
     * Need confirmation
     * @param stockPrice
     * @return
     */
    public boolean isBearishHarami(StockPrice stockPrice);

    /**
     * Need confirmation
     * @param stockPrice
     * @return
     */
    public boolean isBullishHarami(StockPrice stockPrice);

    /**
     * Need confirmation
     * @param stockPrice
     * @return
     */
    public boolean isBullishInsideBar(StockPrice stockPrice);

    /**
     * Need confirmation
     * @param stockPrice
     * @return
     */
    public boolean isBearishInsideBar(StockPrice stockPrice);


    public boolean isThreeWhiteSoldiers(StockPrice stockPrice);

    public boolean isThreeBlackCrows(StockPrice stockPrice);

    public boolean isThreeInsideUp(StockPrice stockPrice);

    public boolean isThreeInsideDown(StockPrice stockPrice);

    public boolean isThreeOutsideUp(StockPrice stockPrice);

    public boolean isThreeOutsideDown(StockPrice stockPrice);

    public boolean isMorningStar(StockPrice stockPrice);

    public boolean isEveningStar(StockPrice stockPrice);

    public boolean isRisingThreeMethods(StockPrice stockPrice);

    public boolean isFallingThreeMethods(StockPrice stockPrice);
}
