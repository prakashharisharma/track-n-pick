package com.example.repo.ledger;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.CrossOverLedger;
import com.example.model.ledger.CrossOverLedger.CrossOverCategory;
import com.example.model.ledger.CrossOverLedger.CrossOverType;
import com.example.model.ledger.CrossOverLedger.Status;
import com.example.model.master.Stock;

@Transactional
@Repository
public interface CrossOverLedgerRepository extends JpaRepository<CrossOverLedger, Long> {
	
	List<CrossOverLedger> findByStockId(Stock stock);
	
	CrossOverLedger findByStockIdAndCrossOverType(Stock stock,CrossOverType crossOverType);
	
	CrossOverLedger findByStockIdAndCrossOverTypeAndCrossOverCategoryAndStatus(Stock stock,CrossOverType crossOverType, CrossOverCategory crossOverCategory, Status status);

	List<CrossOverLedger> findByResearchDate(LocalDate researchDate);
	
	List<CrossOverLedger> findByStockIdAndStatusOrderByResearchDateDesc(Stock stock,Status status);

	List<CrossOverLedger> findTop5ByCrossOverTypeAndCrossOverCategoryAndStatusOrderByResearchDateDesc(CrossOverType crossOverType,CrossOverCategory crossOverCategory,Status status);
	
}
