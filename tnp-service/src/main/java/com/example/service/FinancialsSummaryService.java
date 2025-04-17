package com.example.service;

import com.example.data.transactional.entities.FinancialsSummary;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.repo.FinancialsSummaryRepository;
import com.example.data.transactional.repo.StockRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FinancialsSummaryService {

    private final FinancialsSummaryRepository financialsSummaryRepository;

    private final StockRepository
            stockRepository; // Assuming you have a Stock repository to fetch stock by ID

    // Create or Update method that accepts FinancialsSummary object
    public FinancialsSummary createOrUpdate(Stock stock, FinancialsSummary financialsSummary) {

        // Check if the FinancialsSummary already exists for the given stock
        Optional<FinancialsSummary> existingSummaryOptional =
                financialsSummaryRepository.findByStock(stock);

        if (existingSummaryOptional.isEmpty()) {
            // If no existing summary, associate the stock and save the new FinancialsSummary
            financialsSummary.setStock(stock);
        } else {
            FinancialsSummary existingSummary = existingSummaryOptional.get();
            // If an existing summary is found, update the existing one
            existingSummary.setIssuedSize(financialsSummary.getIssuedSize());
            existingSummary.setFaceValue(financialsSummary.getFaceValue());
            financialsSummary = existingSummary;
        }

        // Save the entity (create or update)
        return financialsSummaryRepository.save(financialsSummary);
    }
}
