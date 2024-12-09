package com.example.service;

import com.example.model.master.Stock;

public interface BreakoutService {

    public boolean isBreakOut20(Stock stock);
    public boolean isBreakOut50(Stock stock);
    public boolean isBreakOut100(Stock stock);
    public boolean isBreakOut200(Stock stock);

    public boolean isBreakDown20(Stock stock);
    public boolean isBreakDown50(Stock stock);
    public boolean isBreakDown100(Stock stock);
    public boolean isBreakDown200(Stock stock);
}
