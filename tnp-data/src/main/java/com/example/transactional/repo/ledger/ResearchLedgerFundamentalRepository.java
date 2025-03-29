package com.example.transactional.repo.ledger;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.transactional.model.ledger.ResearchLedgerFundamental;
import com.example.transactional.model.master.Stock;
import com.example.util.io.model.ResearchIO.ResearchTrigger;


@Transactional
@Repository
public interface ResearchLedgerFundamentalRepository extends JpaRepository<ResearchLedgerFundamental, Long> {

	ResearchLedgerFundamental findBySrlId(long srlId); 
	
	ResearchLedgerFundamental findByStockAndResearchStatus(Stock stock, ResearchTrigger researchStatus);

	ResearchLedgerFundamental findByStock(Stock stock);
	
	ResearchLedgerFundamental findByStockAndNotified(Stock stock,boolean isNotified);
	
	//ResearchLedgerFundamental findByStockAndNotifiedStorage(Stock stock, ResearchType researchType, boolean notifiedStorage);
	
	//ResearchLedgerFundamental findByStockAndResearchStatusAndNotifiedStorage(Stock stock,  ResearchTrigger researchStatus, boolean notifiedStorage);
	
	List<ResearchLedgerFundamental> findByResearchStatus(ResearchTrigger researchStatus);

	List<ResearchLedgerFundamental> findByNotified(boolean isNotified);
	
	List<ResearchLedgerFundamental> findAll();
	
	List<ResearchLedgerFundamental> findByResearchStatusAndNotified(ResearchTrigger researchStatus,boolean isNotified);
	
}
