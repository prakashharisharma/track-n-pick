package com.example.repo.ledger;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.UndervalueLedger;
import com.example.model.master.Stock;

@Transactional
@Repository
public interface UndervalueLedgerRepository extends JpaRepository<UndervalueLedger, Long>{
	
	UndervalueLedger findByStockId(Stock stock);
	
	List<UndervalueLedger> findByResearchDate(LocalDate researchDate);
}
