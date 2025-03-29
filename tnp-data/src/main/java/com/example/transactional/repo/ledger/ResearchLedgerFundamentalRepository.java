package com.example.transactional.repo.ledger;

import java.util.List;

import javax.transaction.Transactional;

import com.example.transactional.model.um.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.transactional.model.ledger.ResearchLedgerFundamental;
import com.example.transactional.model.master.Stock;


@Transactional
@Repository
public interface ResearchLedgerFundamentalRepository extends JpaRepository<ResearchLedgerFundamental, Long> {

	ResearchLedgerFundamental findBySrlId(long srlId); 
	
	ResearchLedgerFundamental findByStockAndResearchStatus(Stock stock, Trade.Type researchStatus);

	ResearchLedgerFundamental findByStock(Stock stock);
	
	ResearchLedgerFundamental findByStockAndNotified(Stock stock,boolean isNotified);
	
	//ResearchLedgerFundamental findByStockAndNotifiedStorage(Stock stock, ResearchType researchType, boolean notifiedStorage);
	
	//ResearchLedgerFundamental findByStockAndResearchStatusAndNotifiedStorage(Stock stock,  ResearchTrigger researchStatus, boolean notifiedStorage);
	
	List<ResearchLedgerFundamental> findByResearchStatus(Trade.Type researchStatus);

	List<ResearchLedgerFundamental> findByNotified(boolean isNotified);
	
	List<ResearchLedgerFundamental> findAll();
	
	List<ResearchLedgerFundamental> findByResearchStatusAndNotified(Trade.Type researchStatus, boolean isNotified);
	
}
