package com.example.repo.ledger;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.ResearchLedgerFundamental;
import com.example.model.master.Stock;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;



@Transactional
@Repository
public interface ResearchLedgerFundamentalRepository extends JpaRepository<ResearchLedgerFundamental, Long> {

	ResearchLedgerFundamental findBySrlId(long srlId); 
	
	ResearchLedgerFundamental findByStockAndResearchStatus(Stock stock, ResearchTrigger researchStatus);

	ResearchLedgerFundamental findByStock(Stock stock, ResearchType researchType);
	
	ResearchLedgerFundamental findByStockAndNotified(Stock stock, ResearchType researchType,boolean isNotified);
	
	ResearchLedgerFundamental findByStockAndNotifiedStorage(Stock stock, ResearchType researchType, boolean notifiedStorage);
	
	ResearchLedgerFundamental findByStockAndResearchStatusAndNotifiedStorage(Stock stock,  ResearchTrigger researchStatus, boolean notifiedStorage);
	
	List<ResearchLedgerFundamental> findByResearchStatus(ResearchTrigger researchStatus);

	List<ResearchLedgerFundamental> findByNotified(boolean isNotified);
	
	List<ResearchLedgerFundamental> findAll();
	
	List<ResearchLedgerFundamental> findByResearchStatusAndNotified(ResearchTrigger researchStatus,boolean isNotified);
	
}
