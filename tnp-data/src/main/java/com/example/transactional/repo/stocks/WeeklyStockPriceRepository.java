package com.example.transactional.repo.stocks;

import com.example.transactional.model.master.Stock;
import com.example.transactional.model.stocks.StockPriceOld;
import com.example.transactional.model.stocks.WeeklyStockPriceOld;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Transactional
@Repository
public interface WeeklyStockPriceRepository extends JpaRepository<WeeklyStockPriceOld, Long> {

    StockPriceOld findByStock(Stock stock);
}