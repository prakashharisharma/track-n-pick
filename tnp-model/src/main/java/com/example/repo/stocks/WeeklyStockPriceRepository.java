package com.example.repo.stocks;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPriceOld;
import com.example.model.stocks.WeeklyStockPriceOld;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Transactional
@Repository
public interface WeeklyStockPriceRepository extends JpaRepository<WeeklyStockPriceOld, Long> {

    StockPriceOld findByStock(Stock stock);
}