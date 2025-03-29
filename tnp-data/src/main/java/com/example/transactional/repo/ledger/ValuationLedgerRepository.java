package com.example.transactional.repo.ledger;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.transactional.model.ledger.ValuationLedger;
import com.example.transactional.model.ledger.ValuationLedger.Status;
import com.example.transactional.model.ledger.ValuationLedger.Type;
import com.example.transactional.model.master.Stock;

@Transactional
@Repository
public interface ValuationLedgerRepository extends JpaRepository<ValuationLedger, Long>{
	
	List<ValuationLedger> findByStockId(Stock stock);
	
	List<ValuationLedger> findByResearchDate(LocalDate researchDate);
	
	ValuationLedger findByStockIdAndTypeAndStatus(Stock stock,Type type, Status status);

	List<ValuationLedger> findByStockIdAndStatusOrderByResearchDateDesc(Stock stock,Status status);
}
