package com.example.service.utils;

import com.example.data.transactional.entities.StockPrice;

public class RangeActionUtils {

    private static final double ZONE_TOLERANCE = 1.0;

    /** Checks if the price moved down into the support zone and bounced up */
    public static boolean fallsIntoSupportAndBouncesUp(
            StockPrice stockPrice, SupportResistanceZoneUtils.Zone supportZone) {
        return (CandleStickUtils.isGreen(stockPrice)
                        || CandleStickUtils.isPrevStrongLowerWick(stockPrice))
                && stockPrice.getPrevClose() > supportZone.getEnd()
                && stockPrice.getLow() <= supportZone.getEnd();
    }

    /** Checks if the price closes inside or slightly above the support zone */
    public static boolean closesAboveSupportZone(
            StockPrice stockPrice, SupportResistanceZoneUtils.Zone supportZone) {
        return (CandleStickUtils.isGreen(stockPrice)
                        || CandleStickUtils.isPrevStrongLowerWick(stockPrice))
                && stockPrice.getPrevClose() < supportZone.getStart()
                && stockPrice.getClose() >= supportZone.getStart()
                && stockPrice.getClose() <= supportZone.getEnd() + ZONE_TOLERANCE;
    }

    /** Checks if price moved up into resistance zone and got rejected */
    public static boolean risesIntoResistanceAndGetsRejected(
            StockPrice stockPrice, SupportResistanceZoneUtils.Zone resistanceZone) {
        return (CandleStickUtils.isRed(stockPrice)
                        || CandleStickUtils.isStrongUpperWick(stockPrice))
                && stockPrice.getPrevClose() < resistanceZone.getStart()
                && stockPrice.getHigh() >= resistanceZone.getStart();
    }

    /** Checks if price closes inside or just below resistance zone */
    public static boolean closesBelowResistanceZone(
            StockPrice stockPrice, SupportResistanceZoneUtils.Zone resistanceZone) {
        return (CandleStickUtils.isRed(stockPrice)
                        || CandleStickUtils.isStrongUpperWick(stockPrice))
                && stockPrice.getPrevClose() > resistanceZone.getEnd()
                && stockPrice.getClose() <= resistanceZone.getEnd()
                && stockPrice.getClose() >= resistanceZone.getStart() - ZONE_TOLERANCE;
    }
}
