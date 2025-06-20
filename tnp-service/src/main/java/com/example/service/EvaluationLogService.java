package com.example.service;

import com.example.data.transactional.entities.EvaluationLog;
import com.example.data.transactional.entities.EvaluationLog.Type;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.data.transactional.repo.EvaluationLogRepository;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class EvaluationLogService {

    @Autowired private EvaluationLogRepository evaluationLogRepository;

    public void add(StockPrice stockPrice, Type type, String details) {

        EvaluationLog evaluationLog =
                evaluationLogRepository.findByStockAndTimeframeAndTypeAndDetailsAndSessionDate(
                        stockPrice.getStock(),
                        stockPrice.getTimeframe(),
                        type,
                        details,
                        stockPrice.getSessionDate());

        if (evaluationLog == null) {

            evaluationLog =
                    new EvaluationLog(
                            stockPrice.getTimeframe(),
                            stockPrice.getStock(),
                            stockPrice.getSessionDate(),
                            type,
                            details);

            evaluationLogRepository.save(evaluationLog);
        }
    }

    public void add(StockTechnicals stockTechnicals, Type type, String details) {

        EvaluationLog evaluationLog =
                evaluationLogRepository.findByStockAndTimeframeAndTypeAndDetailsAndSessionDate(
                        stockTechnicals.getStock(),
                        stockTechnicals.getTimeframe(),
                        type,
                        details,
                        stockTechnicals.getSessionDate());

        if (evaluationLog == null) {

            evaluationLog =
                    new EvaluationLog(
                            stockTechnicals.getTimeframe(),
                            stockTechnicals.getStock(),
                            stockTechnicals.getSessionDate(),
                            type,
                            details);

            evaluationLogRepository.save(evaluationLog);
        }
    }
}
