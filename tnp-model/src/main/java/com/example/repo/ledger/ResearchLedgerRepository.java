package com.example.repo.ledger;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;

@Transactional
@Repository
public interface ResearchLedgerRepository extends JpaRepository<ResearchLedger, Long> {

	ResearchLedger findByStockAndActive(Stock stock, boolean isActive);
	
	List<ResearchLedger> findAll();
	
	List<ResearchLedger> findByNotified(boolean isNotified);
	
}
