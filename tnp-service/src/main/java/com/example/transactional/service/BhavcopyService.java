package com.example.transactional.service;

import com.example.transactional.model.StockPriceIN;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface BhavcopyService {

    public List<StockPriceIN> downloadAndProcessBhavcopy(LocalDate sessinDate) throws IOException;
}
