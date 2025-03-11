package com.example.service;

import com.example.model.master.Stock;

public interface MacdIndicatorService {
    public boolean isMacdCrossedSignal(Stock stock);

    public boolean isSignalNearHistogram(Stock stock);

    public boolean isHistogramGreen(Stock stock);

    public boolean isHistogramIncreased(Stock stock);

    public boolean isHistogramDecreased(Stock stock);
}
