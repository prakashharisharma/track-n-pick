package com.example.enhanced.service;

import com.example.enhanced.model.StockPriceIN;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface BhavcopyService {

    public List<StockPriceIN> downloadAndProcessBhavcopy(LocalDate sessinDate) throws IOException;
}
