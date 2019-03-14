package com.example.repo.ledger;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.CrossOverLedger;
import com.example.model.ledger.CrossOverLedger.CrossOverType;
import com.example.model.master.Stock;

@Transactional
@Repository
public interface CrossOverLedgerRepository extends JpaRepository<CrossOverLedger, Long> {
	CrossOverLedger findByStockId(Stock stock);
	CrossOverLedger findByStockIdAndCrossOverType(Stock stock,CrossOverType crossOverType);

	List<CrossOverLedger> findByResearchDate(LocalDate researchDate);
}
