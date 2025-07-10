package com.example.service.utils;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockTechnicals;

public class VolumeAverageUtil {
    private VolumeAverageUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Returns the appropriate average volume from StockTechnicals for the given timeframe.
     *
     * @param timeframe DAILY, WEEKLY, or MONTHLY
     * @param stockTechnicals the technicals object containing precomputed averages
     * @return the average volume for that timeframe
     * @throws IllegalArgumentException if an unsupported timeframe is passed or stockTechnicals is
     *     null
     */
    public static long getAverageVolume(Timeframe timeframe, StockTechnicals stockTechnicals) {
        if (stockTechnicals == null) {
            throw new IllegalArgumentException("stockTechnicals must not be null");
        }

        switch (timeframe) {
            case DAILY:
                return stockTechnicals.getVolumeAvg20(); // 20-day average
            case WEEKLY:
                return stockTechnicals.getVolumeAvg10(); // 10-week average
            case MONTHLY:
                return stockTechnicals.getVolumeAvg5(); // 5-month average
            default:
                throw new IllegalArgumentException("Unsupported timeframe: " + timeframe);
        }
    }

    public static long getPrevAverageVolume(Timeframe timeframe, StockTechnicals stockTechnicals) {
        if (stockTechnicals == null) {
            throw new IllegalArgumentException("stockTechnicals must not be null");
        }

        switch (timeframe) {
            case DAILY:
                return stockTechnicals.getPrevVolumeAvg20(); // 20-day average
            case WEEKLY:
                return stockTechnicals.getPrevVolumeAvg10(); // 10-week average
            case MONTHLY:
                return stockTechnicals.getPrevVolumeAvg5(); // 5-month average
            default:
                throw new IllegalArgumentException("Unsupported timeframe: " + timeframe);
        }
    }
}
