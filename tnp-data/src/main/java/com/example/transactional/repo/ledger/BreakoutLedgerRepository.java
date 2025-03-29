package com.example.transactional.repo.ledger;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.transactional.model.ledger.BreakoutLedger;
import com.example.transactional.model.ledger.BreakoutLedger.BreakoutCategory;
import com.example.transactional.model.ledger.BreakoutLedger.BreakoutType;
import com.example.transactional.model.master.Stock;

@Transactional
@Repository
public interface BreakoutLedgerRepository extends JpaRepository<BreakoutLedger, Long>{
	List<BreakoutLedger> findByStockId(Stock stock);
	List<BreakoutLedger> findByBreakoutDate(LocalDate researchDate);
	
	List<BreakoutLedger> findByStockIdOrderByBreakoutDateDesc(Stock stock);
	
	BreakoutLedger findByStockIdAndBreakoutTypeAndBreakoutCategory(Stock stock,BreakoutType breakoutType, BreakoutCategory breakoutCategory);
	
	BreakoutLedger findByStockIdAndBreakoutTypeAndBreakoutCategoryAndBreakoutDate(Stock stock,BreakoutType breakoutType, BreakoutCategory breakoutCategory, LocalDate breakoutDate);

	// BreakoutLedger findByStockIdAndBreakoutTypeAndBreakoutCategoryAndPriceAndSma(Stock stock,BreakoutType breakoutType, BreakoutCategory breakoutCategory, double price, double sma);
}
