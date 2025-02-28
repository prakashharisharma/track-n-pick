package com.example.service;

import com.example.model.master.Stock;

public interface CandleStickHelperService {

    public boolean isUpperWickSizeConfirmed(Stock stock);
    public boolean isLowerWickSizeConfirmed(Stock stock);
    public boolean isBullishConfirmed(Stock stock, boolean isWickCheck);

    public boolean isBearishConfirmed(Stock stock, boolean isWickCheck);

}
